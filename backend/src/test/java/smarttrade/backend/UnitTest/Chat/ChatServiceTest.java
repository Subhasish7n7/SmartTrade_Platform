package smarttrade.backend.UnitTest.Chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import smarttrade.backend.Mappers.ChatMapper;
import smarttrade.backend.dto.chat.ChatConversationResponse;
import smarttrade.backend.dto.chat.ChatEvent;
import smarttrade.backend.dto.chat.ChatEventType;
import smarttrade.backend.dto.chat.ChatMessageResponse;
import smarttrade.backend.entities.chat.ChatConversationEntity;
import smarttrade.backend.entities.chat.ChatMessageEntity;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.user.*;
import smarttrade.backend.repository.chat.ChatConversationRepo;
import smarttrade.backend.repository.chat.ChatMessageRepo;
import smarttrade.backend.repository.trades.TradeOfferRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.ChatService;
import smarttrade.backend.service.NotificationService;
import smarttrade.backend.service.user.UserPreferenceService;
import smarttrade.backend.websocket.ActiveConversationRegistry;
import smarttrade.backend.websocket.ChatPresencePublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @Mock
    private ChatMessageRepo chatMessageRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private ChatConversationRepo conversationRepo;
    @Mock
    private TradeRepo tradeRepo;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ActiveConversationRegistry activeConversationRegistry;
    @Mock
    private ChatMapper chatMapper;
    @Mock
    private ChatPresencePublisher presencePublisher;
    @Mock
    private TradeOfferRepo tradeOfferRepo;
    @Mock
    private UserPreferenceService userPreferenceService;

    @InjectMocks
    private ChatService chatService;

    private UserEntity user;
    private UserEntity otherUser;
    private UserEntity outsider;

    @BeforeEach
    void setUp() {
        user = user(1L, "user@test.com", "User");
        otherUser = user(2L, "other@test.com", "Other");
        outsider = user(3L, "outsider@test.com", "Outsider");
    }

    private UserPreferenceEntity defaultPreferences(UserEntity owner) {
        LocalDateTime now = LocalDateTime.now();

        return UserPreferenceEntity.builder()
                .user(owner)
                .profileVisibility(ProfileVisibility.PUBLIC)
                .locationVisibility(LocationVisibility.CITY)
                .inventoryVisibility(InventoryVisibility.PUBLIC)
                .showOnlineStatus(true)
                .acceptTradeRequests(true)
                .acceptBuyOffers(true)
                .messagePermission(MessagePermission.EVERYONE)
                .minimumTraderTrustScore(0)
                .showUnavailableListings(false)
                .defaultListingType(DefaultListingType.SALE)
                .notifyTrades(true)
                .notifyMessages(true)
                .notifySystem(true)
                .loginAlerts(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void shouldSendMessageAndCreateConversationWhenConversationDoesNotExist() {
        when(userPreferenceService.getForUser(otherUser))
                .thenReturn(defaultPreferences(otherUser));
        ChatConversationEntity conversation = conversation(10L, user, otherUser);

        ChatMessageEntity savedMessage = message(
                100L, user, conversation, "Hello"
        );

        ChatMessageResponse messageResponse = ChatMessageResponse.builder()
                .id(100L).conversationId(10L).senderId(1L)
                .senderName("User").message("Hello").build();

        ChatConversationResponse receiverConversation =
                ChatConversationResponse.builder()
                        .conversationId(10L).otherUserId(1L)
                        .otherUserName("User").unreadCount(1).build();

        ChatConversationResponse senderConversation =
                ChatConversationResponse.builder()
                        .conversationId(10L).otherUserId(2L)
                        .otherUserName("Other").unreadCount(0).build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));
        when(conversationRepo.findBetweenUsers(1L, 2L))
                .thenReturn(Optional.empty());
        when(conversationRepo.save(any(ChatConversationEntity.class)))
                .thenReturn(conversation);
        when(chatMessageRepo.save(any(ChatMessageEntity.class)))
                .thenReturn(savedMessage);
        when(chatMapper.toMessageResponse(savedMessage))
                .thenReturn(messageResponse);
        when(tradeRepo.countActiveTradesBetweenUsers(1L, 2L))
                .thenReturn(1);
        when(chatMessageRepo.countUnread(10L, 2L, null))
                .thenReturn(1);
        when(chatMapper.toConversationResponse(
                conversation, otherUser, savedMessage, 1, 1))
                .thenReturn(receiverConversation);
        when(chatMapper.toConversationResponse(
                conversation, user, savedMessage, 1, 0))
                .thenReturn(senderConversation);

        chatService.sendMessage(2L, "Hello");

        ArgumentCaptor<ChatConversationEntity> conversationCaptor =
                ArgumentCaptor.forClass(ChatConversationEntity.class);
        verify(conversationRepo).save(conversationCaptor.capture());

        ChatConversationEntity created = conversationCaptor.getValue();
        assertEquals(1L, created.getUser1().getUserId());
        assertEquals(2L, created.getUser2().getUserId());

        ArgumentCaptor<ChatMessageEntity> messageCaptor =
                ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(chatMessageRepo).save(messageCaptor.capture());

        ChatMessageEntity createdMessage = messageCaptor.getValue();
        assertSame(user, createdMessage.getSender());
        assertSame(conversation, createdMessage.getConversation());
        assertEquals("Hello", createdMessage.getMessage());

        ArgumentCaptor<ChatEvent> eventCaptor =
                ArgumentCaptor.forClass(ChatEvent.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq("other@test.com"), eq("/queue/messages"), eventCaptor.capture()
        );
        verify(messagingTemplate).convertAndSendToUser(
                eq("user@test.com"), eq("/queue/messages"), any(ChatEvent.class)
        );

        ChatEvent receiverEvent = eventCaptor.getValue();
        assertEquals(ChatEventType.MESSAGE_CREATED, receiverEvent.getType());
        assertSame(messageResponse, receiverEvent.getMessage());
        assertSame(receiverConversation, receiverEvent.getConversation());

        verify(notificationService).notifyChatMessage(
                user, otherUser, conversation.getId()
        );
    }

    @Test
    void shouldSendMessageUsingExistingConversation() {
        when(userPreferenceService.getForUser(otherUser))
                .thenReturn(defaultPreferences(otherUser));
        ChatConversationEntity conversation = conversation(10L, user, otherUser);
        ChatMessageEntity savedMessage =
                message(100L, user, conversation, "Hello");

        ChatMessageResponse messageResponse =
                ChatMessageResponse.builder().id(100L).message("Hello").build();

        ChatConversationResponse mapped =
                ChatConversationResponse.builder().conversationId(10L).build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));
        when(conversationRepo.findBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(conversation));
        when(chatMessageRepo.save(any(ChatMessageEntity.class)))
                .thenReturn(savedMessage);
        when(chatMapper.toMessageResponse(savedMessage))
                .thenReturn(messageResponse);
        when(tradeRepo.countActiveTradesBetweenUsers(1L, 2L))
                .thenReturn(0);
        when(chatMessageRepo.countUnread(10L, 2L, null))
                .thenReturn(0);
        when(chatMapper.toConversationResponse(
                conversation, otherUser, savedMessage, 0, 0))
                .thenReturn(mapped);
        when(chatMapper.toConversationResponse(
                conversation, user, savedMessage, 0, 0))
                .thenReturn(mapped);

        chatService.sendMessage(2L, "Hello");

        verify(conversationRepo, never()).save(any(ChatConversationEntity.class));
        verify(chatMessageRepo).save(any(ChatMessageEntity.class));
        verify(notificationService).notifyChatMessage(user, otherUser, 10L);
    }

    @Test
    void shouldRejectSendingMessageToUnknownUser() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.sendMessage(999L, "Hello")
        );

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(conversationRepo, chatMessageRepo, messagingTemplate);
    }

    @Test
    void shouldGetConversationAndMarkItRead() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);
        ChatMessageEntity latest = message(100L, otherUser, conversation, "Latest");

        Page<ChatMessageEntity> page =
                new PageImpl<>(List.of(latest), PageRequest.of(0, 40), 1);

        ChatMessageResponse mapped =
                ChatMessageResponse.builder().id(100L).message("Latest").build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));
        when(conversationRepo.findBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(conversation));
        when(chatMessageRepo.findByConversation_IdOrderByCreatedAtDesc(
                10L, PageRequest.of(0, 40)))
                .thenReturn(page);
        when(chatMessageRepo.findTopByConversation_IdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(latest));
        when(chatMapper.toMessageResponse(latest)).thenReturn(mapped);

        Page<ChatMessageResponse> result =
                chatService.getConversation(2L, 0, 40);

        assertEquals(1, result.getTotalElements());
        assertSame(mapped, result.getContent().getFirst());
        assertSame(latest, conversation.getUser1LastReadMessage());
        verify(conversationRepo).save(conversation);
    }

    @Test
    void shouldCapConversationPageSizeAt50() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));
        when(conversationRepo.findBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(conversation));
        when(chatMessageRepo.findByConversation_IdOrderByCreatedAtDesc(
                10L, PageRequest.of(1, 50)))
                .thenReturn(Page.empty(PageRequest.of(1, 50)));
        when(chatMessageRepo.findTopByConversation_IdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.empty());

        Page<ChatMessageResponse> result =
                chatService.getConversation(2L, 1, 500);

        assertTrue(result.isEmpty());
        verify(chatMessageRepo).findByConversation_IdOrderByCreatedAtDesc(
                10L, PageRequest.of(1, 50)
        );
        verify(conversationRepo, never()).save(any(ChatConversationEntity.class));
    }

    @Test
    void shouldReturnEmptyPageWhenConversationDoesNotExist() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));
        when(conversationRepo.findBetweenUsers(1L, 2L))
                .thenReturn(Optional.empty());

        Page<ChatMessageResponse> result =
                chatService.getConversation(2L, 3, 40);

        assertTrue(result.isEmpty());
        assertEquals(3, result.getNumber());
        assertEquals(40, result.getSize());
        verifyNoInteractions(chatMessageRepo, chatMapper);
    }

    @Test
    void shouldRejectConversationWhenOtherUserDoesNotExist() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.getConversation(999L, 0, 40)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldGetInboxSortedByLatestMessage() {
        when(tradeOfferRepo.findBySender_UserIdOrReceiver_UserId(
                anyLong(),
                anyLong()
        )).thenReturn(List.of());
        ChatConversationEntity olderConversation =
                conversation(10L, user, otherUser);
        ChatConversationEntity newerConversation =
                conversation(20L, user, outsider);

        ChatMessageEntity olderMessage =
                message(100L, otherUser, olderConversation, "Older");
        olderMessage.setCreatedAt(LocalDateTime.of(2026, 9, 20, 10, 0));

        ChatMessageEntity newerMessage =
                message(200L, outsider, newerConversation, "Newer");
        newerMessage.setCreatedAt(LocalDateTime.of(2026, 9, 21, 10, 0));

        ChatConversationResponse olderResponse =
                ChatConversationResponse.builder()
                        .conversationId(10L)
                        .otherUserId(2L)
                        .lastMessageAt(olderMessage.getCreatedAt())
                        .build();

        ChatConversationResponse newerResponse =
                ChatConversationResponse.builder()
                        .conversationId(20L)
                        .otherUserId(3L)
                        .lastMessageAt(newerMessage.getCreatedAt())
                        .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(conversationRepo.findByUser1_UserIdOrUser2_UserId(1L, 1L))
                .thenReturn(List.of(olderConversation, newerConversation));
        when(chatMessageRepo.findTopByConversation_IdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(olderMessage));
        when(chatMessageRepo.findTopByConversation_IdOrderByCreatedAtDesc(20L))
                .thenReturn(Optional.of(newerMessage));
        when(tradeRepo.countActiveTradesBetweenUsers(1L, 2L)).thenReturn(1);
        when(tradeRepo.countActiveTradesBetweenUsers(1L, 3L)).thenReturn(0);
        when(chatMessageRepo.countUnread(10L, 1L, null)).thenReturn(2);
        when(chatMessageRepo.countUnread(20L, 1L, null)).thenReturn(1);
        when(chatMapper.toConversationResponse(
                olderConversation, user, olderMessage, 1, 2))
                .thenReturn(olderResponse);
        when(chatMapper.toConversationResponse(
                newerConversation, user, newerMessage, 0, 1))
                .thenReturn(newerResponse);

        List<ChatConversationResponse> result = chatService.getInbox(null);

        assertEquals(2, result.size());
        assertSame(newerResponse, result.get(0));
        assertSame(olderResponse, result.get(1));
    }

    @Test
    void shouldPinValidTradeToConversation() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);
        TradeEntity trade = TradeEntity.builder()
                .tradeId(50L)
                .initiator(user)
                .receiver(otherUser)
                .status(TradeStatus.OPEN)
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));
        when(conversationRepo.findBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(conversation));
        when(tradeRepo.findById(50L)).thenReturn(Optional.of(trade));

        chatService.pinTrade(2L, 50L);

        assertSame(trade, conversation.getPinnedTrade());
        verify(conversationRepo).save(conversation);
    }

    @Test
    void shouldRejectPinningTradeOutsideConversation() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);
        UserEntity thirdUser = user(3L, "third@test.com", "Third");

        TradeEntity trade = TradeEntity.builder()
                .tradeId(50L)
                .initiator(user)
                .receiver(thirdUser)
                .status(TradeStatus.OPEN)
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));
        when(conversationRepo.findBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(conversation));
        when(tradeRepo.findById(50L)).thenReturn(Optional.of(trade));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.pinTrade(2L, 50L)
        );

        assertEquals("Trade does not belong to this conversation",
                exception.getMessage());
        verify(conversationRepo, never()).save(any(ChatConversationEntity.class));
    }

    @Test
    void shouldUpdatePresenceAndPublishOnlineWhenUserBecomesActive() {
        when(userPreferenceService.getForUser(user))
                .thenReturn(defaultPreferences(user));
        ChatConversationEntity conversation = conversation(10L, user, otherUser);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(conversationRepo.findById(10L)).thenReturn(Optional.of(conversation));
        when(activeConversationRegistry.isViewingConversation(1L, 10L))
                .thenReturn(false);

        chatService.updatePresence("session-1", 10L);

        verify(activeConversationRegistry).updatePresence("session-1", 1L, 10L);
        verify(presencePublisher).publish(10L, 1L, true);
    }

    @Test
    void shouldNotPublishOnlineAgainWhenUserAlreadyActive() {
        when(userPreferenceService.getForUser(user))
                .thenReturn(defaultPreferences(user));
        ChatConversationEntity conversation = conversation(10L, user, otherUser);

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(conversationRepo.findById(10L)).thenReturn(Optional.of(conversation));
        when(activeConversationRegistry.isViewingConversation(1L, 10L))
                .thenReturn(true);

        chatService.updatePresence("session-2", 10L);

        verify(activeConversationRegistry).updatePresence("session-2", 1L, 10L);
        verify(presencePublisher, never()).publish(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void shouldRejectPresenceUpdateForNonParticipant() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);

        when(authenticatedUserService.getCurrentUser()).thenReturn(outsider);
        when(conversationRepo.findById(10L)).thenReturn(Optional.of(conversation));

        assertThrows(
                AccessDeniedException.class,
                () -> chatService.updatePresence("session-1", 10L)
        );

        verifyNoInteractions(activeConversationRegistry, presencePublisher);
    }

    @Test
    void shouldMarkConversationReadForUser1() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);
        ChatMessageEntity latest = message(100L, otherUser, conversation, "Latest");

        when(chatMessageRepo.findTopByConversation_IdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(latest));

        chatService.markConversationRead(conversation, user);

        assertSame(latest, conversation.getUser1LastReadMessage());
        verify(conversationRepo).save(conversation);
    }

    @Test
    void shouldMarkConversationReadForUser2() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);
        ChatMessageEntity latest = message(100L, user, conversation, "Latest");

        when(chatMessageRepo.findTopByConversation_IdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(latest));

        chatService.markConversationRead(conversation, otherUser);

        assertSame(latest, conversation.getUser2LastReadMessage());
        verify(conversationRepo).save(conversation);
    }

    @Test
    void shouldDoNothingWhenConversationHasNoMessages() {
        ChatConversationEntity conversation = conversation(10L, user, otherUser);

        when(chatMessageRepo.findTopByConversation_IdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.empty());

        chatService.markConversationRead(conversation, user);

        verify(conversationRepo, never()).save(any(ChatConversationEntity.class));
    }

    @Test
    void shouldGetUser() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(otherUser));

        assertSame(otherUser, chatService.getUser(2L));
    }

    @Test
    void shouldRejectGetUserForUnknownId() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> chatService.getUser(999L)
        );

        assertEquals("User not found", exception.getMessage());
    }

    private UserEntity user(Long id, String email, String name) {
        return UserEntity.builder()
                .userId(id).email(email).name(name).password("password").build();
    }

    private ChatConversationEntity conversation(
            Long id, UserEntity user1, UserEntity user2) {
        return ChatConversationEntity.builder()
                .id(id).user1(user1).user2(user2).build();
    }

    private ChatMessageEntity message(
            Long id, UserEntity sender,
            ChatConversationEntity conversation, String text) {
        return ChatMessageEntity.builder()
                .id(id).sender(sender).conversation(conversation)
                .message(text).createdAt(LocalDateTime.now()).build();
    }
}
