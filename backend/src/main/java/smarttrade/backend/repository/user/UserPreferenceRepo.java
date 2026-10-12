package smarttrade.backend.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.user.UserPreferenceEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepo extends JpaRepository<UserPreferenceEntity, Long> {
    Optional<UserPreferenceEntity> findByUser_UserId(Long userId);

    List<UserPreferenceEntity> findAllByUser_UserIdIn(Collection<Long> userIds);
}
