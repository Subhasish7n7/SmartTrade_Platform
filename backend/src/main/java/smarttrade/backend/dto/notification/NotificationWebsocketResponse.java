package smarttrade.backend.dto.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationWebsocketResponse {

    private NotificationResponse notification;

    private Long unreadCount;

    private Long tradeAlertCount;

    private Long chatAlertCount;
}