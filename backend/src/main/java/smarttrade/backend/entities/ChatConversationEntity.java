package smarttrade.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_conversations",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user1_id", "user2_id"})}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id")
    private UserEntity user1;

    @ManyToOne
    @JoinColumn(name = "user2_id")
    private UserEntity user2;

    @ManyToOne
    @JoinColumn(name = "pinned_trade_id")
    private TradeEntity pinnedTrade;

    private LocalDateTime createdAt;
}