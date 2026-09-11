package smarttrade.backend.dto.chat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatEvent {
    private ChatEventType type;
    private ChatMessageResponse message;
    private ChatConversationResponse conversation;

}