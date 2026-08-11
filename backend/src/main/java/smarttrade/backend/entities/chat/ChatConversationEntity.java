package smarttrade.backend.entities.chat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.UserEntity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_last_read_message_id")
    private ChatMessageEntity user1LastReadMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_last_read_message_id")
    private ChatMessageEntity user2LastReadMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}