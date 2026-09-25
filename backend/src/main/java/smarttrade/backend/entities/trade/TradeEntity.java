package smarttrade.backend.entities.trade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import smarttrade.backend.entities.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tradeId;

    @ManyToOne
    private UserEntity initiator;

    @ManyToOne
    private UserEntity receiver;

    @Enumerated(EnumType.STRING)
    private TradeStatus status;

    /*
     * Two-step completion confirmation.
     */

    @Column(nullable = false)
    @Builder.Default
    private Boolean initiatorCompletionConfirmed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean receiverCompletionConfirmed = false;

    private LocalDateTime completionRequestedAt;

    private LocalDateTime createdAt;
}
