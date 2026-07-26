package smarttrade.backend.entities.notification;

import jakarta.persistence.*;
import lombok.*;
import smarttrade.backend.entities.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user_created",
                        columnList = "recipient_id,createdAt"),

                @Index(name = "idx_notification_user_read",
                        columnList = "recipient_id,isRead")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User receiving the notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserEntity recipient;

    /**
     * User responsible for triggering it.
     * Can be null for SYSTEM notifications.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private UserEntity actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationResourceType resourceType;

    /**
     * Primary resource id.
     *
     * Trade
     * Item
     * Conversation
     * Offer
     */
    private Long resourceId;

    /**
     * Human-readable message.
     */
    @Column(nullable = false,length = 250)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    private LocalDateTime readAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}