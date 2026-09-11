package smarttrade.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final ActiveConversationRegistry registry;
    private final ChatPresencePublisher presencePublisher;

    @EventListener
    public void disconnect(SessionDisconnectEvent event) {

        ActiveConversationSession session = registry.disconnect(event.getSessionId());

        if (session == null) {
            return;
        }

        boolean stillActive =
                registry.isUserActiveInConversation(
                        session.getUserId(),
                        session.getConversationId()
                );

        if (!stillActive) {
            presencePublisher.publish(
                    session.getConversationId(),
                    session.getUserId(),
                    false
            );
        }
    }
}