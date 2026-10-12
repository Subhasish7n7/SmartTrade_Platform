package smarttrade.backend.repository.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.user.UserRole;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT u
            FROM UserEntity u
            WHERE u.userId = :userId
            """)
    Optional<UserEntity> findByIdForUpdate(
            @Param("userId") Long userId
    );

    long countByAccountStatus(AccountStatus accountStatus);

    long countByRole(UserRole role);

    long countByTrustScoreLessThan(double threshold);

    @Query("SELECT AVG(u.trustScore) FROM UserEntity u")
    Double findAverageTrustScore();

    @Query("""
            SELECT u
            FROM UserEntity u
            WHERE
                (
                    LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                AND (:role IS NULL OR u.role = :role)
                AND (:status IS NULL OR u.accountStatus = :status)
            ORDER BY u.createdAt DESC
            """)
    Page<UserEntity> searchAdminUsers(
            @Param("search") String search,
            @Param("role") UserRole role,
            @Param("status") AccountStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT u
            FROM UserEntity u
            WHERE
                (:role IS NULL OR u.role = :role)
                AND (:status IS NULL OR u.accountStatus = :status)
            ORDER BY u.createdAt DESC
            """)
    Page<UserEntity> searchAdminUsersByFilters(
            @Param("role") UserRole role,
            @Param("status") AccountStatus status,
            Pageable pageable
    );


    Page<UserEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
