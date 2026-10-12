package smarttrade.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveConversationSession {
    private String sessionId;
    private Long userId;
    private Long conversationId;
}
