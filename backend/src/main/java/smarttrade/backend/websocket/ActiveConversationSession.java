package smarttrade.backend.websocket;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActiveConversationSession {
    private String sessionId;
    private Long userId;
    private Long conversationId;
}