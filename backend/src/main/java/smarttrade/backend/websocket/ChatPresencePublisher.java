package smarttrade.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.chat.ChatPresenceEvent;
import smarttrade.backend.dto.chat.ChatPresenceEventType;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.chat.ChatConversationEntity;
import smarttrade.backend.repository.chat.ChatConversationRepo;

@Component
@RequiredArgsConstructor
public class ChatPresencePublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatConversationRepo conversationRepo;
    private final ActiveConversationRegistry registry;

    public void publish(Long conversationId, Long userId, boolean online) {

        ChatConversationEntity conversation = conversationRepo.findById(conversationId).orElse(null);

        if (conversation == null) {
            return;
        }

        ChatPresenceEvent event =
                ChatPresenceEvent.builder()
                        .type(ChatPresenceEventType.PRESENCE_UPDATED)
                        .conversationId(conversationId)
                        .userId(userId)
                        .online(online)
                        .build();

        UserEntity user1 = conversation.getUser1();
        UserEntity user2 = conversation.getUser2();

        messagingTemplate.convertAndSendToUser(
                user1.getEmail(),
                "/queue/presence",
                event
        );

        messagingTemplate.convertAndSendToUser(
                user2.getEmail(),
                "/queue/presence",
                event
        );
    }
}