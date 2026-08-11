package smarttrade.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {
    private final ActiveConversationRegistry registry;
    @EventListener
    public void disconnect(SessionDisconnectEvent event) {
        registry.disconnect(event.getSessionId());
    }
}
