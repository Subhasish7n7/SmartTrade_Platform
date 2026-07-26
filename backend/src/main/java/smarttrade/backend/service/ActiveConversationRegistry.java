package smarttrade.backend.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveConversationRegistry {

    /*
        userId -> conversationId currently open
     */
    private final Map<Long, Long> activeConversations =
            new ConcurrentHashMap<>();

    public void enterConversation(Long userId, Long conversationId) {

        activeConversations.put(userId, conversationId);
    }

    public void leaveConversation(Long userId) {

        activeConversations.remove(userId);
    }

    public boolean isViewingConversation(
            Long userId,
            Long conversationId
    ) {

        Long active = activeConversations.get(userId);

        return active != null &&
                active.equals(conversationId);
    }

}