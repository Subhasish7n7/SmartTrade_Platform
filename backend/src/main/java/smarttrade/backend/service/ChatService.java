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
import smarttrade.backend.entities.chat.ChatConversationEntity;
import smarttrade.backend.entities.chat.ChatMessageEntity;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeOfferEntity;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.chat.ChatConversationRepo;
import smarttrade.backend.repository.chat.ChatMessageRepo;
import smarttrade.backend.repository.trades.TradeOfferRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.user.UserPreferenceService;
import smarttrade.backend.entities.user.UserPreferenceEntity;
import smarttrade.backend.entities.user.MessagePermission;
import smarttrade.backend.websocket.ActiveConversationRegistry;
import smarttrade.backend.websocket.ChatPresencePublisher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final TradeOfferRepo tradeOfferRepo;
    private final UserPreferenceService userPreferenceService;

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

        boolean visible = userPreferenceService.getForUser(current).isShowOnlineStatus();
        if (!visible) {
            presencePublisher.publish(conversationId, current.getUserId(), false);
        } else if (!alreadyActive) {
            presencePublisher.publish(conversationId, current.getUserId(), true);
        }
    }

    public List<ChatConversationResponse> getInbox(Long requestedOtherUserId) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        Long currentUserId = currentUser.getUserId();

        Map<Long, ChatConversationResponse> responsesByUser = new HashMap<>();
        Map<Long, LocalDateTime> activityByUser = new HashMap<>();

        /*
         * 1. Existing conversations.
         */
        List<ChatConversationEntity> conversations =
                conversationRepo.findByUser1_UserIdOrUser2_UserId(
                        currentUserId,
                        currentUserId
                );

        for (ChatConversationEntity conversation : conversations) {

            ChatMessageEntity lastMessage =
                    chatMessageRepo
                            .findTopByConversation_IdOrderByCreatedAtDesc(conversation.getId())
                            .orElse(null);

            UserEntity otherUser =
                    conversation.getUser1().getUserId().equals(currentUserId)
                            ? conversation.getUser2()
                            : conversation.getUser1();

            Integer activeTradeCount =
                    tradeRepo.countActiveTradesBetweenUsers(
                            currentUserId,
                            otherUser.getUserId()
                    );

            Integer unreadCount =
                    getUnreadCount(conversation, currentUser);

            ChatConversationResponse response =
                    chatMapper.toConversationResponse(
                            conversation,
                            currentUser,
                            lastMessage,
                            activeTradeCount,
                            unreadCount
                    );

            Long otherUserId = otherUser.getUserId();

            responsesByUser.put(otherUserId, response);

            /*
             * Normal chat activity is determined by the latest real message.
             * If a legacy conversation somehow has no message, use the
             * conversation creation time as a safe fallback.
             */
            LocalDateTime activityTime =
                    lastMessage != null
                            ? lastMessage.getCreatedAt()
                            : conversation.getCreatedAt();

            activityByUser.put(otherUserId, activityTime);
        }

        /*
         * 2. Trade/buy-offer participants.
         *
         * Both normal trade offers and buy offers are TradeOfferEntity,
         * so one query covers both.
         */
        List<TradeOfferEntity> offers =
                tradeOfferRepo.findBySender_UserIdOrReceiver_UserId(
                        currentUserId,
                        currentUserId
                );

        /*
         * Keep only the latest offer for each user pair.
         * This is mainly important for legacy offers that may not have
         * created a conversation/message.
         */
        Map<Long, TradeOfferEntity> latestOfferByUser = new HashMap<>();

        for (TradeOfferEntity offer : offers) {

            Long otherUserId =
                    offer.getSender().getUserId().equals(currentUserId)
                            ? offer.getReceiver().getUserId()
                            : offer.getSender().getUserId();

            TradeOfferEntity existing =
                    latestOfferByUser.get(otherUserId);

            if (existing == null
                    || offer.getCreatedAt().isAfter(existing.getCreatedAt())) {

                latestOfferByUser.put(otherUserId, offer);
            }
        }

        for (Map.Entry<Long, TradeOfferEntity> entry :
                latestOfferByUser.entrySet()) {

            Long otherUserId = entry.getKey();
            TradeOfferEntity latestOffer = entry.getValue();

            LocalDateTime offerActivity = latestOffer.getCreatedAt();

            /*
             * Normal/new case:
             *
             * createTradeOffer/createBuyOffer now creates a chat message,
             * so this participant already exists in responsesByUser.
             *
             * We only need the offer timestamp as a fallback for legacy
             * records that somehow have no conversation/message.
             */
            if (responsesByUser.containsKey(otherUserId)) {

                LocalDateTime currentActivity =
                        activityByUser.get(otherUserId);

                if (currentActivity == null
                        || offerActivity.isAfter(currentActivity)) {

                    /*
                     * Legacy safety:
                     * if an offer exists later than the latest message,
                     * use the offer as the ordering activity.
                     *
                     * For newly-created offers the message timestamp and
                     * offer timestamp will naturally be almost identical.
                     */
                    activityByUser.put(otherUserId, offerActivity);
                }

                continue;
            }

            /*
             * Legacy offer with no ChatConversation / ChatMessage.
             *
             * Do NOT create a fake conversation here.
             * The frontend can still show the user and let them open
             * the chat. The first real message will create the
             * conversation through ChatService.sendMessage().
             */
            UserEntity otherUser =
                    latestOffer.getSender().getUserId().equals(currentUserId)
                            ? latestOffer.getReceiver()
                            : latestOffer.getSender();

            Integer activeTradeCount =
                    tradeRepo.countActiveTradesBetweenUsers(
                            currentUserId,
                            otherUserId
                    );

            ChatConversationResponse response =
                    ChatConversationResponse.builder()
                            .conversationId(null)
                            .otherUserId(otherUser.getUserId())
                            .otherUserName(otherUser.getName())
                            .avatarUrl(
                                    otherUser.getAvatar() != null
                                            ? otherUser.getAvatar().getSecureUrl()
                                            : null
                            )
                            .lastMessage(null)
                            .lastMessageAt(offerActivity)
                            .unreadCount(0)
                            .activeTradeCount(activeTradeCount)
                            .pinnedTradeId(null)
                            .build();

            responsesByUser.put(otherUserId, response);
            activityByUser.put(otherUserId, offerActivity);
        }

        /*
         * 3. Direct profile/chat navigation.
         *
         * The frontend can request the inbox for a specific user, for example:
         *
         *     GET /chat/inbox?otherUserId=42
         *
         * If that user is already present because of:
         *   - an existing conversation, or
         *   - an offer,
         *
         * then nothing new needs to be created.
         *
         * If they are not present at all, create a temporary inbox response.
         *
         * This does NOT create a ChatConversationEntity.
         * The real conversation is created when the first message is sent.
         */
        if (requestedOtherUserId != null
                && !requestedOtherUserId.equals(currentUserId)
                && !responsesByUser.containsKey(requestedOtherUserId)) {

            UserEntity otherUser =
                    userRepo.findById(requestedOtherUserId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("User not found: " + requestedOtherUserId)
                            );

            Integer activeTradeCount =
                    tradeRepo.countActiveTradesBetweenUsers(
                            currentUserId,
                            requestedOtherUserId
                    );

            ChatConversationResponse response =
                    ChatConversationResponse.builder()
                            .conversationId(null)
                            .otherUserId(otherUser.getUserId())
                            .otherUserName(otherUser.getName())
                            .avatarUrl(
                                    otherUser.getAvatar() != null
                                            ? otherUser.getAvatar().getSecureUrl()
                                            : null
                            )
                            .lastMessage(null)
                            .lastMessageAt(null)
                            .unreadCount(0)
                            .activeTradeCount(activeTradeCount)
                            .pinnedTradeId(null)
                            .build();

            responsesByUser.put(requestedOtherUserId, response);
        }

        /*
         * 4. One inbox entry per user.
         *
         * If a specific user was requested by the frontend,
         * that user is always placed first.
         *
         * Otherwise, normal latest-activity ordering is used.
         */
        return responsesByUser.entrySet()
                .stream()
                .sorted(
                        (left, right) -> {

                            /*
                             * Requested user always comes first.
                             */
                            if (requestedOtherUserId != null) {

                                boolean leftIsRequested =
                                        left.getKey()
                                                .equals(requestedOtherUserId);

                                boolean rightIsRequested =
                                        right.getKey()
                                                .equals(requestedOtherUserId);

                                if (leftIsRequested && !rightIsRequested) {
                                    return -1;
                                }

                                if (!leftIsRequested && rightIsRequested) {
                                    return 1;
                                }
                            }

                            /*
                             * Normal inbox ordering:
                             * latest activity first.
                             */
                            LocalDateTime leftActivity =
                                    activityByUser.get(left.getKey());

                            LocalDateTime rightActivity =
                                    activityByUser.get(right.getKey());

                            if (leftActivity == null
                                    && rightActivity == null) {
                                return 0;
                            }

                            if (leftActivity == null) {
                                return 1;
                            }

                            if (rightActivity == null) {
                                return -1;
                            }

                            return rightActivity.compareTo(leftActivity);
                        }
                )
                .map(Map.Entry::getValue)
                .toList();
    }


    public void sendMessage(Long otherUserId, String message) {
        sendMessageInternal(otherUserId, message, false);
    }

    /**
     * System-generated initial messages attached to a valid trade/buy offer bypass direct-chat restrictions.
     */
    public void sendTradeMessage(Long otherUserId, String message) {
        sendMessageInternal(otherUserId, message, true);
    }

    private void sendMessageInternal(Long otherUserId, String message, boolean marketplaceMessage) {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        UserEntity otherUser = userRepo.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!marketplaceMessage) {
            UserPreferenceEntity preferences = userPreferenceService.getForUser(otherUser);
            MessagePermission permission = preferences.getMessagePermission();
            if (permission == MessagePermission.NOBODY) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "This user is not accepting direct messages.");
            }
            if (permission == MessagePermission.TRADERS_ONLY) {
                Integer activeTrades = tradeRepo.countActiveTradesBetweenUsers(
                        currentUser.getUserId(), otherUser.getUserId());
                if (activeTrades == null || activeTrades <= 0) {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "This user only accepts messages from traders.");
                }
            }
        }

        ChatConversationEntity conversation = getOrCreateConversation(currentUser, otherUser);
        ChatMessageEntity chatMessage = ChatMessageEntity.builder()
                .sender(currentUser)
                .conversation(conversation)
                .message(message)
                .build();
        chatMessage = chatMessageRepo.save(chatMessage);

        ChatMessageResponse response = chatMapper.toMessageResponse(chatMessage);
        Integer activeTradeCount = tradeRepo.countActiveTradesBetweenUsers(
                currentUser.getUserId(), otherUser.getUserId());
        Integer receiverUnreadCount = getUnreadCount(conversation, otherUser);
        ChatConversationResponse receiverConversation = chatMapper.toConversationResponse(
                conversation, otherUser, chatMessage, activeTradeCount, receiverUnreadCount);
        ChatConversationResponse senderConversation = chatMapper.toConversationResponse(
                conversation, currentUser, chatMessage, activeTradeCount, 0);
        ChatEvent receiverEvent = createChatEvent(response, receiverConversation);
        ChatEvent senderEvent = createChatEvent(response, senderConversation);

        messagingTemplate.convertAndSendToUser(otherUser.getEmail(), "/queue/messages", receiverEvent);
        messagingTemplate.convertAndSendToUser(currentUser.getEmail(), "/queue/messages", senderEvent);
        notificationService.notifyChatMessage(currentUser, otherUser, conversation.getId());
    }

    /**
     * Re-publishes presence visibility for all conversations currently active for this user.
     */
    public void refreshCurrentPresenceVisibility() {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        boolean visible = userPreferenceService.getForUser(currentUser).isShowOnlineStatus();
        for (Long conversationId : activeConversationRegistry.getActiveConversationIds(currentUser.getUserId())) {
            presencePublisher.publish(conversationId, currentUser.getUserId(), visible);
        }
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

    private ChatEvent createChatEvent(ChatMessageResponse message, ChatConversationResponse conversation) {
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
