package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.ChatMessageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity>
    findByConversation_IdOrderByTimestampAsc(
            Long conversationId
    );
    Optional<ChatMessageEntity>
    findTopByConversation_IdOrderByTimestampDesc(
            Long conversationId
    );
}