package smarttrade.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.chat.ChatInboxResponse;
import smarttrade.backend.dto.chat.ChatMessageResponse;
import smarttrade.backend.entities.*;
import smarttrade.backend.repository.ChatConversationRepo;
import smarttrade.backend.repository.ChatMessageRepo;
import smarttrade.backend.repository.TradeRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepo chatMessageRepo;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final ChatConversationRepo conversationRepo;
    private final TradeRepo tradeRepo;

    private ChatConversationEntity getOrCreateConversation(UserEntity user1, UserEntity user2) {

        Long first = Math.min(user1.getUserId(), user2.getUserId());
        Long second = Math.max(user1.getUserId(), user2.getUserId());

        return conversationRepo
                .findBetweenUsers(first, second)
                .orElseGet(() -> {
                    UserEntity firstUser = first.equals(user1.getUserId()) ? user1 : user2;

                    UserEntity secondUser = second.equals(user1.getUserId()) ? user1 : user2;

                    return conversationRepo.save(
                            ChatConversationEntity.builder()
                                    .user1(firstUser)
                                    .user2(secondUser)
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );
                });
    }

    public List<ChatInboxResponse> getInbox() {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        List<ChatConversationEntity> conversations =
                conversationRepo.findByUser1_UserIdOrUser2_UserId(
                        currentUser.getUserId(), currentUser.getUserId());

        return conversations.stream()
                .map(conversation -> {
                    UserEntity otherUser = conversation.getUser1().getUserId().equals(currentUser.getUserId())
                            ? conversation.getUser2() : conversation.getUser1();

                    ChatMessageEntity lastMessage = chatMessageRepo
                            .findTopByConversation_IdOrderByTimestampDesc(conversation.getId())
                            .orElse(null);

                    Integer activeTradeCount = tradeRepo.countActiveTradesBetweenUsers(currentUser.getUserId(), otherUser.getUserId());

                    return ChatInboxResponse.builder()
                            .otherUserId(otherUser.getUserId())
                            .otherUserName(otherUser.getName())
                            .avatarUrl(null)
                            .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                            .lastMessageAt(lastMessage != null ? lastMessage.getTimestamp() : null)
                            .unreadCount(0)
                            .activeTradeCount(activeTradeCount)
                            .pinnedTradeId(conversation.getPinnedTrade() != null ? conversation.getPinnedTrade().getTradeId() : null)
                            .build();
                })
                .sorted(Comparator.comparing(ChatInboxResponse::getLastMessageAt
                        , Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .toList();
    }

    public ChatMessageResponse saveMessage(Long otherUserId, String message) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        UserEntity otherUser = userRepo.findById(otherUserId).orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        ChatConversationEntity conversation = getOrCreateConversation(currentUser, otherUser);

        ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                .sender(currentUser)
                .conversation(conversation)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        chatMessage = chatMessageRepo.save(chatMessage);

        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .senderId(currentUser.getUserId())
                .senderName(currentUser.getName())
                .message(chatMessage.getMessage())
                .timestamp(chatMessage.getTimestamp())
                .build();
    }

    public UserEntity getUser(Long userId) {

        return userRepo.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        ));
    }

    public List<ChatMessageResponse> getConversation(Long otherUserId) {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        UserEntity otherUser = userRepo.findById(otherUserId).orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        ChatConversationEntity conversation = findConversation(currentUser, otherUser)
                        .orElse(null);

        if (conversation == null) {
            return List.of();
        }

        return chatMessageRepo
                .findByConversation_IdOrderByTimestampAsc(conversation.getId())
                .stream()
                .map(message ->
                        ChatMessageResponse.builder()
                                .id(message.getId())
                                .senderId(message.getSender().getUserId())
                                .senderName(message.getSender().getName())
                                .message(message.getMessage())
                                .timestamp(message.getTimestamp())
                                .build()
                )
                .toList();
    }

    public void pinTrade(Long otherUserId, Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        UserEntity otherUser = getUser(otherUserId);

        ChatConversationEntity conversation = getOrCreateConversation(currentUser, otherUser);

        TradeEntity trade = tradeRepo.findById(tradeId).orElseThrow(() ->
                new IllegalArgumentException("Trade not found"));

        boolean validTrade =
                (trade.getInitiator().getUserId().equals(currentUser.getUserId())
                        &&
                        trade.getReceiver().getUserId().equals(otherUser.getUserId()))
                        ||
                        (trade.getInitiator().getUserId().equals(otherUser.getUserId())
                                &&
                                trade.getReceiver().getUserId().equals(currentUser.getUserId()));

        if (!validTrade) {
            throw new IllegalArgumentException(
                    "Trade does not belong to this conversation");
        }

        conversation.setPinnedTrade(trade);
        conversationRepo.save(conversation);
    }
    private Optional<ChatConversationEntity> findConversation(UserEntity user1, UserEntity user2) {

        Long first = Math.min(user1.getUserId(), user2.getUserId());

        Long second = Math.max(user1.getUserId(), user2.getUserId());

        return conversationRepo.findBetweenUsers(first, second);
    }
}