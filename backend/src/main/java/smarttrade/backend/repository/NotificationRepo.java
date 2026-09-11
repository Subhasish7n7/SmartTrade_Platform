package smarttrade.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.notification.NotificationEntity;
import smarttrade.backend.entities.notification.NotificationResourceType;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationEntity,Long> {

    Page<NotificationEntity> findByRecipient_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Long countByRecipient_UserIdAndIsReadFalse(Long userId);

    @Query("""
    SELECT COUNT(n)
    FROM NotificationEntity n
    WHERE n.recipient.userId = :userId
      AND n.isRead = false
      AND n.resourceType = :resourceType
""")
    long countUnreadByResourceType(Long userId, NotificationResourceType resourceType);

    @Modifying
    @Query("""
    update NotificationEntity n
    set
        n.isRead=true,
        n.readAt=CURRENT_TIMESTAMP
    where
        n.id=:id
    and
        n.recipient.userId=:userId
    and
        n.isRead=false
    """)
    int markRead(Long id, Long userId);

    @Modifying
    @Query("""
    delete
    from NotificationEntity n
    where
        n.id=:id
    and
        n.recipient.userId=:userId
    """)
    int deleteOwned(Long id, Long userId);

    @Modifying
    @Query("""
        update NotificationEntity n
        set
            n.isRead=true,
            n.readAt=CURRENT_TIMESTAMP
        where
            n.recipient.userId=:userId
        and
            n.isRead=false
        """)
    void markAllRead(Long userId);

    @Modifying
    @Query("""
        UPDATE NotificationEntity n
        SET n.isRead = true,
            n.readAt = CURRENT_TIMESTAMP
        WHERE n.recipient.userId = :userId
          AND n.resourceType = :resourceType
          AND n.isRead = false
    """)
    int markModuleRead(Long userId, NotificationResourceType resourceType);
}