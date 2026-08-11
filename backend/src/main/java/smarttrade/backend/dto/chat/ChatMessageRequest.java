package smarttrade.backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMessageRequest {
    @NotNull
    private Long otherUserId;

    @NotBlank
    @Size(max = 2000)
    private String message;
}