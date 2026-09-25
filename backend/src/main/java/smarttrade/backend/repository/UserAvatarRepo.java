package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.user.UserAvatarEntity;

@Repository
public interface UserAvatarRepo extends JpaRepository<UserAvatarEntity, Long> {
}