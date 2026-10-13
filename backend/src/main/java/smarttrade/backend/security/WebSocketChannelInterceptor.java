package smarttrade.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final String WS_TICKET_HEADER = "X-WS-TICKET";

    private final WebSocketTicketService webSocketTicketService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String ticket = accessor.getFirstNativeHeader(WS_TICKET_HEADER);
            if (ticket == null || ticket.isBlank()) {
                throw new AccessDeniedException("WebSocket authentication ticket is required");
            }

            Authentication authentication =
                    webSocketTicketService.consumeAndAuthenticate(ticket);
            accessor.setUser(authentication);
        }

        return message;
    }
}