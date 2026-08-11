package smarttrade.backend.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatConversationResponse {
    private Long conversationId;
    private Long otherUserId;
    private String otherUserName;
    private String avatarUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private Integer activeTradeCount;
    private Long pinnedTradeId;
}