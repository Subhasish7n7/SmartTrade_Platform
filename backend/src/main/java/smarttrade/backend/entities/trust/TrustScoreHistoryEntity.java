package smarttrade.backend.entities.trust;

import jakarta.persistence.*;
import lombok.*;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "trust_score_history",
        indexes = {
                @Index(
                        name = "idx_trust_history_user_created",
                        columnList = "user_id,created_at"
                ),
                @Index(
                        name = "idx_trust_history_source",
                        columnList = "source_type,source_id"
                )
        }
)
public class TrustScoreHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TrustEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TrustEventSourceType sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "related_user_id")
    private Long relatedUserId;

    @Column(nullable = false)
    private double scoreBefore;

    @Column(nullable = false)
    private double scoreAfter;

    @Column(nullable = false)
    private double scoreChange;

    @Column(nullable = false)
    private double transactionComponent;

    @Column(nullable = false)
    private double reviewComponent;

    @Column(nullable = false)
    private double conductComponent;

    @Column(nullable = false)
    private double verificationComponent;

    @Column(nullable = false)
    private double listingQualityComponent;

    @Column(nullable = false)
    private double maturityComponent;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}