package smarttrade.backend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.notification.NotificationEntity;

@Repository
public interface NotificationRepo
        extends JpaRepository<NotificationEntity,Long> {

    Page<NotificationEntity> findByRecipient_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Long countByRecipient_UserIdAndIsReadFalse(Long userId);

    @Modifying
    @Transactional
    @Query("""
        update NotificationEntity n
        set
            n.isRead=true,
            n.readAt=CURRENT_TIMESTAMP
        where
            n.id=:id
            and
            n.isRead=false
        """)
    int markRead(Long id);

    @Modifying
    @Transactional
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
}