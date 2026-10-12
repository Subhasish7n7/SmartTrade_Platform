package smarttrade.backend.service.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.user.UserAvatarRequest;
import smarttrade.backend.entities.user.UserAvatarEntity;
import smarttrade.backend.entities.user.UserAvatarStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.avatarImageCleanup.UserAvatarCleanupRequestedEvent;
import smarttrade.backend.repository.user.UserAvatarRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final UserAvatarRepo userAvatarRepo;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void replaceAvatar(UserEntity user, UserAvatarRequest request) {
        validateAvatarRequest(request);

        UserAvatarEntity currentAvatar =
                userAvatarRepo.findByUser_UserIdAndStatus(
                        user.getUserId(),
                        UserAvatarStatus.ACTIVE
                ).orElse(null);

        /*
         * The client may send the currently persisted avatar again
         * when saving other profile fields.
         *
         * Do not delete and recreate the same Cloudinary asset.
         */
        if (currentAvatar != null
                && currentAvatar.getAssetId() != null
                && currentAvatar.getAssetId().equals(request.getAssetId())) {

            updateMetadataIfNecessary(currentAvatar, request);
            return;
        }

        /*
         * Make sure the same Cloudinary asset is not already associated
         * with another avatar row.
         */
        userAvatarRepo.findByAssetId(request.getAssetId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(
                            currentAvatar == null ? null : currentAvatar.getId()
                    )) {
                        throw new IllegalArgumentException(
                                "Cloudinary asset is already associated with an avatar"
                        );
                    }
                });

        LocalDateTime now = LocalDateTime.now();

        /*
         * Detach the old avatar and mark it for external cleanup.
         *
         * This does NOT delete Cloudinary data inside the transaction.
         */
        if (currentAvatar != null) {
            currentAvatar.setStatus(UserAvatarStatus.PENDING_DELETION);
            currentAvatar.setMarkedForDeletionAt(now);
            currentAvatar.setUser(null);

            userAvatarRepo.save(currentAvatar);
        }

        /*
         * Create the new active avatar.
         */
        UserAvatarEntity newAvatar = UserAvatarEntity.builder()
                .user(user)
                .secureUrl(request.getSecureUrl())
                .publicId(request.getPublicId())
                .assetId(request.getAssetId())
                .width(request.getWidth())
                .height(request.getHeight())
                .format(request.getFormat())
                .bytes(request.getBytes())
                .status(UserAvatarStatus.ACTIVE)
                .createdAt(now)
                .build();

        userAvatarRepo.save(newAvatar);

        /*
         * Only the old avatar requires Cloudinary cleanup.
         */
        if (currentAvatar != null) {
            applicationEventPublisher.publishEvent(
                    new UserAvatarCleanupRequestedEvent(
                            List.of(currentAvatar.getId())
                    )
            );
        }
    }

    @Transactional
    public void removeAvatar(UserEntity user) {

        UserAvatarEntity currentAvatar =
                userAvatarRepo.findByUser_UserIdAndStatus(
                        user.getUserId(),
                        UserAvatarStatus.ACTIVE
                ).orElse(null);

        if (currentAvatar == null) {
            return;
        }

        currentAvatar.setStatus(UserAvatarStatus.PENDING_DELETION);
        currentAvatar.setMarkedForDeletionAt(LocalDateTime.now());
        currentAvatar.setUser(null);

        userAvatarRepo.saveAndFlush(currentAvatar);

        applicationEventPublisher.publishEvent(
                new UserAvatarCleanupRequestedEvent(
                        List.of(currentAvatar.getId())
                )
        );
    }

    private void updateMetadataIfNecessary(
            UserAvatarEntity avatar,
            UserAvatarRequest request
    ) {
        avatar.setSecureUrl(request.getSecureUrl());
        avatar.setPublicId(request.getPublicId());
        avatar.setWidth(request.getWidth());
        avatar.setHeight(request.getHeight());
        avatar.setFormat(request.getFormat());
        avatar.setBytes(request.getBytes());
        userAvatarRepo.save(avatar);
    }

    private void validateAvatarRequest(UserAvatarRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Avatar cannot be null");
        }

        if (isBlank(request.getSecureUrl())
                || isBlank(request.getPublicId())
                || isBlank(request.getAssetId())
                || isBlank(request.getFormat())) {
            throw new IllegalArgumentException(
                    "Cloudinary avatar metadata is incomplete"
            );
        }

        if (request.getWidth() == null || request.getWidth() <= 0
                || request.getHeight() == null || request.getHeight() <= 0
                || request.getBytes() == null || request.getBytes() <= 0) {
            throw new IllegalArgumentException(
                    "Cloudinary avatar dimensions and size must be valid"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}