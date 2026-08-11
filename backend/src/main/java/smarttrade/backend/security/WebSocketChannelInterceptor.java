package smarttrade.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            Map<String, Object> session = accessor.getSessionAttributes();

            if (session == null || !(session.get("authentication") instanceof Authentication authentication)) {
                throw new IllegalArgumentException("Unauthenticated WebSocket connection");
            }

            accessor.setUser(authentication);
        }

        return message;
    }

}