package smarttrade.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private LocalDateTime createdAt;
}
