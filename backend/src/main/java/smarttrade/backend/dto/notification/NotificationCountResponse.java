package smarttrade.backend.dto.notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationCountResponse {

    private Long unreadCount;

    private Long tradeAlertCount;

    private Long chatAlertCount;
}