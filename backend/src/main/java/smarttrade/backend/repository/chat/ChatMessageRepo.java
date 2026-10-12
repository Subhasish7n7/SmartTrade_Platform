package smarttrade.backend.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.chat.ChatMessageEntity;

import java.util.Optional;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessageEntity, Long> {
    Page<ChatMessageEntity> findByConversation_IdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    Optional<ChatMessageEntity> findTopByConversation_IdOrderByCreatedAtDesc(Long conversationId);

    @Query("""
            SELECT COUNT(m)
            FROM ChatMessageEntity m
            WHERE
                m.conversation.id = :conversationId
            AND
                m.sender.userId <> :viewerId
            AND
                (:lastReadId IS NULL OR m.id > :lastReadId)
            """)
    int countUnread(Long conversationId, Long viewerId, Long lastReadId);
}