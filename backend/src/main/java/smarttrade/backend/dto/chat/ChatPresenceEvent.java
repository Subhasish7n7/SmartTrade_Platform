package smarttrade.backend.dto.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatPresenceEvent {

    private ChatPresenceEventType type;

    private Long conversationId;

    private Long userId;

    private boolean online;
}