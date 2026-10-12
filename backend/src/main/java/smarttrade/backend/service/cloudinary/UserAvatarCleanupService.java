package smarttrade.backend.service.cloudinary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.user.UserAvatarEntity;
import smarttrade.backend.entities.user.UserAvatarStatus;
import smarttrade.backend.repository.user.UserAvatarRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAvatarCleanupService {

    private final UserAvatarRepo userAvatarRepo;
    private final CloudinaryAssetService cloudinaryAssetService;

    @Async("cloudinaryTaskExecutor")
    public void cleanupAsync(List<Long> avatarIds) {
        if (avatarIds == null || avatarIds.isEmpty()) {
            return;
        }

        avatarIds.forEach(this::cleanupOne);
    }

    @Scheduled(
            fixedDelayString = "${cloudinary.cleanup.fixed-delay-ms:3600000}"
    )
    public void cleanupPendingBatch() {
        List<UserAvatarEntity> pending =
                userAvatarRepo.findTop50ByStatusOrderByMarkedForDeletionAtAsc(
                        UserAvatarStatus.PENDING_DELETION
                );

        pending.forEach(avatar -> cleanupOne(avatar.getId()));
    }

    private void cleanupOne(Long avatarId) {

        UserAvatarEntity avatar =
                userAvatarRepo.findById(avatarId).orElse(null);

        if (avatar == null
                || avatar.getStatus() != UserAvatarStatus.PENDING_DELETION) {
            return;
        }

        String assetId = avatar.getAssetId();

        if (assetId == null || assetId.isBlank()) {
            log.error(
                    "Pending avatar {} has no Cloudinary asset ID; leaving DB row for repair",
                    avatarId
            );
            return;
        }

        try {
            cloudinaryAssetService.deleteByAssetId(assetId);
            userAvatarRepo.deleteById(avatarId);

            log.info(
                    "Deleted Cloudinary avatar asset {} and DB avatar {}",
                    assetId,
                    avatarId
            );

        } catch (Exception e) {
            log.warn(
                    "Cloudinary deletion failed for avatar {} asset {}; leaving row pending",
                    avatarId,
                    assetId,
                    e
            );
        }
    }
}