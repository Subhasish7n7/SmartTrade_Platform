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
@Table(name = "trades")
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradeId;

    @ManyToOne
    private UserEntity initiator;

    @ManyToOne
    private UserEntity receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean initiatorCompletionConfirmed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean receiverCompletionConfirmed = false;

    private LocalDateTime completionRequestedAt;

    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime disputedAt;

    @ManyToOne
    @JoinColumn(name = "cancelled_by_user_id")
    private UserEntity cancelledBy;
}