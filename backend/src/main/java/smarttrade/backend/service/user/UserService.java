package smarttrade.backend.service.user;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.user.UpdateMyProfileRequest;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserAvatarService userAvatarService;

    public UserService(
            UserRepo userRepo,
            AuthenticatedUserService authenticatedUserService,
            UserAvatarService userAvatarService
    ) {
        this.userRepo = userRepo;
        this.authenticatedUserService = authenticatedUserService;
        this.userAvatarService = userAvatarService;
    }

    public List<UserEntity> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<UserEntity> GetUser(Long user_Id) {
        return userRepo.findById(user_Id);
    }

    public UserEntity addUser(UserEntity user) {
        return userRepo.save(user);
    }

    public UserEntity updateUser(Long userId, UserEntity user) {
        user.setUserId(userId);
        return userRepo.save(user);
    }

    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }

    @Transactional
    public UserEntity updateMyProfile(UpdateMyProfileRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Profile update request cannot be null"
            );
        }

        UserEntity authenticatedUser = authenticatedUserService.getCurrentUser();

        /*
         * Lock the actual database row rather than trusting a user ID
         * supplied by the client.
         */
        UserEntity user = userRepo.findByIdForUpdate(authenticatedUser.getUserId())
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user no longer exists")
                );

        /*
         * Profile fields.
         *
         * Email/password are intentionally not handled by this endpoint.
         */
        if (request.getName() != null) {
            String name = request.getName().trim();

            if (name.isBlank()) {
                throw new IllegalArgumentException(
                        "Name cannot be blank"
                );
            }

            user.setName(name);
        }

        if (request.getPhoneNo() != null) {
            user.setPhone_no(request.getPhoneNo().trim());
        }

        /*
         * Avatar state:
         *
         *   avatar != null
         *       -> replace
         *
         *   clearAvatar = true
         *       -> remove
         *
         *   neither
         *       -> unchanged
         */
        if (request.getAvatar() != null
                && request.isClearAvatar()) {
            throw new IllegalArgumentException(
                    "Avatar cannot be provided while clearAvatar is true"
            );
        }

        if (request.getAvatar() != null) {
            userAvatarService.replaceAvatar(user, request.getAvatar());

        } else if (request.isClearAvatar()) {
            userAvatarService.removeAvatar(user);
        }

        return userRepo.save(user);
    }
}