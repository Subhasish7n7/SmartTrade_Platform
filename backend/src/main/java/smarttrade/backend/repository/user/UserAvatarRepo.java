package smarttrade.backend.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.user.UserAvatarEntity;
import smarttrade.backend.entities.user.UserAvatarStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAvatarRepo extends JpaRepository<UserAvatarEntity, Long> {

    Optional<UserAvatarEntity> findByUser_UserId(Long userId);

    Optional<UserAvatarEntity> findByUser_UserIdAndStatus(
            Long userId,
            UserAvatarStatus status
    );

    Optional<UserAvatarEntity> findByAssetId(String assetId);

    boolean existsByAssetId(String assetId);

    List<UserAvatarEntity> findTop50ByStatusOrderByMarkedForDeletionAtAsc(
            UserAvatarStatus status
    );
}