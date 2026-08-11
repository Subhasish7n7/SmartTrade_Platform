package smarttrade.backend.websocket;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ActiveConversationRegistry {

    /*
        sessionId -> session
     */
    private final ConcurrentHashMap<String, ActiveConversationSession> sessions = new ConcurrentHashMap<>();

    /*
        conversationId
            ↓
        userId
            ↓
        active websocket session count
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, AtomicInteger>> conversationPresence =
            new ConcurrentHashMap<>();

    public void updatePresence(String sessionId, Long userId, Long conversationId) {

        ActiveConversationSession previous = sessions.get(sessionId);

        if (previous != null) {
            removePresence(previous);
        }

        ActiveConversationSession current =
                ActiveConversationSession.builder()
                        .sessionId(sessionId)
                        .userId(userId)
                        .conversationId(conversationId)
                        .build();

        sessions.put(sessionId, current);

        conversationPresence
                .computeIfAbsent(conversationId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, id -> new AtomicInteger())
                .incrementAndGet();
    }

    public void disconnect(String sessionId) {

        ActiveConversationSession removed =
                sessions.remove(sessionId);

        if (removed != null) {
            removePresence(removed);
        }
    }

    public boolean isViewingConversation(Long userId, Long conversationId) {

        ConcurrentHashMap<Long, AtomicInteger> users = conversationPresence.get(conversationId);

        if (users == null) {
            return false;
        }

        AtomicInteger count = users.get(userId);

        return count != null && count.get() > 0;
    }

    private void removePresence(ActiveConversationSession session) {

        ConcurrentHashMap<Long, AtomicInteger> users = conversationPresence.get(session.getConversationId());

        if (users == null) {
            return;
        }

        AtomicInteger counter = users.get(session.getUserId());

        if (counter == null) {
            return;
        }

        if (counter.decrementAndGet() <= 0) {
            users.remove(session.getUserId());
        }

        if (users.isEmpty()) {
            conversationPresence.remove(session.getConversationId());
        }
    }

}