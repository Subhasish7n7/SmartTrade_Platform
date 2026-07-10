package smarttrade.backend.dto.chat;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long otherUserId;
    private String message;
}