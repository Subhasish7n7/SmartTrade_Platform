package smarttrade.backend.repository.auth;

import jakarta.transaction.Transactional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.auth.PasswordResetTokenEntity;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetTokenEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetTokenEntity t where t.tokenHash = :tokenHash and t.usedAt is null")
    Optional<PasswordResetTokenEntity> findUnusedByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Transactional
    @Query("delete from PasswordResetTokenEntity t where t.user.userId = :userId and t.usedAt is null")
    void deleteUnusedForUser(@Param("userId") Long userId);
}
