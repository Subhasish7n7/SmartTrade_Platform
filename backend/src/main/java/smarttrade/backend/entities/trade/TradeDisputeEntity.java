package smarttrade.backend.entities.trade;

import jakarta.persistence.*;
import lombok.*;
import smarttrade.backend.entities.user.UserEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trade_disputes")
public class TradeDisputeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispute_id")
    private Long disputeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_id", nullable = false)
    private TradeEntity trade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opened_by_user_id", nullable = false)
    private UserEntity openedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private UserEntity resolvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fault_user_id")
    private UserEntity faultUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisputeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DisputeReason reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private DisputeResolution resolution;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime underReviewAt;
    private LocalDateTime resolvedAt;
}