package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import smarttrade.backend.Mappers.ChatMapper;
import smarttrade.backend.dto.chat.ChatConversationResponse;
import smarttrade.backend.dto.chat.ChatEvent;
import smarttrade.backend.dto.chat.ChatEventType;
import smarttrade.backend.dto.chat.ChatMessageResponse;
import smarttrade.backend.entities.*;
import smarttrade.backend.entities.chat.ChatConversationEntity;
import smarttrade.backend.entities.chat.ChatMessageEntity;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.repository.ChatConversationRepo;
import smarttrade.backend.repository.ChatMessageRepo;
import smarttrade.backend.repository.TradeRepo;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.websocket.ActiveConversationRegistry;
import smarttrade.backend.websocket.ChatPresencePublisher;

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
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final ActiveConversationRegistry activeConversationRegistry;
    private final ChatMapper chatMapper;
    private final ChatPresencePublisher presencePublisher;

    private ChatConversationEntity getOrCreateConversation(UserEntity user1, UserEntity user2) {

        Long first = Math.min(user1.getUserId(), user2.getUserId());
        Long second = Math.max(user1.getUserId(), user2.getUserId());

        return conversationRepo.findBetweenUsers(first, second)
                .orElseGet(() -> {
                    UserEntity firstUser = first.equals(user1.getUserId()) ? user1 : user2;

                    UserEntity secondUser = second.equals(user1.getUserId()) ? user1 : user2;

                    return conversationRepo.save(
                            ChatConversationEntity.builder()
                                    .user1(firstUser)
                                    .user2(secondUser)
                                    .build()
                    );
                });
    }
    public void updatePresence(String sessionId, Long conversationId) {

        UserEntity current = authenticatedUserService.getCurrentUser();

        ChatConversationEntity conversation =
                conversationRepo.findById(conversationId)
                        .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        boolean participant =
                conversation.getUser1().getUserId()
                        .equals(current.getUserId())
                        ||
                        conversation.getUser2().getUserId()
                                .equals(current.getUserId());

        if (!participant) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access denied"
            );
        }

        boolean alreadyActive =
                activeConversationRegistry.isViewingConversation(
                        current.getUserId(),
                        conversationId
                );

        activeConversationRegistry.updatePresence(
                sessionId,
                current.getUserId(),
                conversationId
        );

        if (!alreadyActive) {
            presencePublisher.publish(
                    conversationId,
                    current.getUserId(),
                    true
            );
        }
    }
    public List<ChatConversationResponse> getInbox() {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        List<ChatConversationEntity> conversations =
                conversationRepo.findByUser1_UserIdOrUser2_UserId(
                        currentUser.getUserId(), currentUser.getUserId());

        return conversations.stream()
                .map(conversation -> {
                    ChatMessageEntity lastMessage = chatMessageRepo
                            .findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId())
                                    .orElse(null);

                    UserEntity otherUser = conversation.getUser1()
                                    .getUserId()
                                    .equals(currentUser.getUserId())
                                    ?
                                    conversation.getUser2()
                                    :
                                    conversation.getUser1();

                    Integer activeTradeCount = tradeRepo
                            .countActiveTradesBetweenUsers(currentUser.getUserId(), otherUser.getUserId());

                    Integer unreadCount = getUnreadCount(conversation, currentUser);

                    return chatMapper.toConversationResponse(
                            conversation,
                            currentUser,
                            lastMessage,
                            activeTradeCount,
                            unreadCount
                    );
                })
                .sorted(
                        Comparator.comparing(
                                ChatConversationResponse::getLastMessageAt,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                )
                .toList();
    }

    public void sendMessage(Long otherUserId, String message) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        UserEntity otherUser = userRepo.findById(otherUserId).orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        ChatConversationEntity conversation = getOrCreateConversation(currentUser, otherUser);

        ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                .sender(currentUser)
                .conversation(conversation)
                .message(message)
                .build();

        chatMessage = chatMessageRepo.save(chatMessage);

        ChatMessageResponse response = chatMapper.toMessageResponse(chatMessage);
        Integer activeTradeCount= tradeRepo
                .countActiveTradesBetweenUsers(currentUser.getUserId(), otherUser.getUserId());


        Integer receiverUnreadCount = getUnreadCount(conversation, otherUser);
        ChatConversationResponse receiverConversation =
                chatMapper.toConversationResponse(
                        conversation, otherUser, chatMessage, activeTradeCount, receiverUnreadCount);

        ChatConversationResponse senderConversation =
                chatMapper.toConversationResponse(
                        conversation, currentUser, chatMessage, activeTradeCount, 0);

        ChatEvent receiverEvent = createChatEvent(response,receiverConversation);

        ChatEvent senderEvent = createChatEvent(response,senderConversation);

        messagingTemplate.convertAndSendToUser(
                otherUser.getEmail(),
                "/queue/messages",
                receiverEvent
        );
        messagingTemplate.convertAndSendToUser(
                currentUser.getEmail(),
                "/queue/messages",
                senderEvent
        );

        notificationService.notifyChatMessage(
                currentUser,
                otherUser,
                conversation.getId()
        );
    }

    public UserEntity getUser(Long userId) {
        return userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public Page<ChatMessageResponse> getConversation(Long otherUserId, int page, int size) {

        size = Math.min(size, 50);

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        UserEntity otherUser = userRepo.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatConversationEntity conversation = findConversation(currentUser, otherUser)
                .orElse(null);

        if (conversation == null) {
            return Page.empty(PageRequest.of(page, size));
        }

        Page<ChatMessageEntity> messages = chatMessageRepo
                .findByConversation_IdOrderByCreatedAtDesc(conversation.getId(),
                        PageRequest.of(page, size));

        markConversationRead(conversation, currentUser);

        return messages.map(chatMapper::toMessageResponse);
    }

    public void pinTrade(Long otherUserId, Long tradeId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        UserEntity otherUser = getUser(otherUserId);

        ChatConversationEntity conversation = getOrCreateConversation(currentUser, otherUser);

        TradeEntity trade = tradeRepo.findById(tradeId).orElseThrow(() ->
                new IllegalArgumentException("Trade not found"));

        boolean validTrade = (trade.getInitiator().getUserId().equals(currentUser.getUserId())
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
    @Transactional
    public void markConversationRead(ChatConversationEntity conversation, UserEntity currentUser) {

        ChatMessageEntity latest = chatMessageRepo
                .findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId())
                        .orElse(null);

        if (latest == null) {
            return;
        }

        if (conversation.getUser1().getUserId().equals(currentUser.getUserId())) {

            conversation.setUser1LastReadMessage(latest);

        } else {
            conversation.setUser2LastReadMessage(latest);
        }

        conversationRepo.save(conversation);
    }
    private Integer getUnreadCount(ChatConversationEntity conversation, UserEntity currentUser) {

        Long lastReadId = null;

        if (conversation.getUser1().getUserId()
                .equals(currentUser.getUserId())) {

            if (conversation.getUser1LastReadMessage() != null) {
                lastReadId = conversation.getUser1LastReadMessage().getId();
            }

        } else {

            if (conversation.getUser2LastReadMessage() != null) {
                lastReadId = conversation.getUser2LastReadMessage().getId();
            }
        }

        return chatMessageRepo.countUnread(
                conversation.getId(),
                currentUser.getUserId(),
                lastReadId
        );
    }
    private ChatEvent createChatEvent(ChatMessageResponse message, ChatConversationResponse conversation){
        return ChatEvent.builder()
                .type(ChatEventType.MESSAGE_CREATED)
                .conversation(conversation)
                .message(message)
                .build();

    }
    private Optional<ChatConversationEntity> findConversation(UserEntity user1, UserEntity user2) {

        Long first = Math.min(user1.getUserId(), user2.getUserId());

        Long second = Math.max(user1.getUserId(), user2.getUserId());

        return conversationRepo.findBetweenUsers(first, second);
    }
}