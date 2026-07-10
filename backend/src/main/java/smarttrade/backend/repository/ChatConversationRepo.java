package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.ChatConversationEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepo
        extends JpaRepository<ChatConversationEntity, Long> {

    @Query("""
        SELECT c
        FROM ChatConversationEntity c
        WHERE
            (c.user1.userId = :user1
                AND c.user2.userId = :user2)
            OR
            (c.user1.userId = :user2
                AND c.user2.userId = :user1)
    """)
    Optional<ChatConversationEntity> findBetweenUsers(
            Long user1,
            Long user2
    );

    List<ChatConversationEntity> findByUser1_UserIdOrUser2_UserId(Long user1, Long user2);
}