package smarttrade.backend.UnitTest.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import smarttrade.backend.Mappers.NotificationMapper;
import smarttrade.backend.dto.notification.NotificationCountResponse;
import smarttrade.backend.dto.notification.NotificationResponse;
import smarttrade.backend.dto.notification.NotificationWebsocketResponse;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.notification.NotificationEntity;
import smarttrade.backend.entities.notification.NotificationResourceType;
import smarttrade.backend.entities.notification.NotificationType;
import smarttrade.backend.repository.NotificationRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.NotificationService;
import smarttrade.backend.service.user.UserPreferenceService;
import smarttrade.backend.websocket.ActiveConversationRegistry;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepo notificationRepo;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private ActiveConversationRegistry activeConversationRegistry;

    @Mock
    private UserPreferenceService userPreferenceService;


    @InjectMocks
    private NotificationService notificationService;

    private UserEntity actor;
    private UserEntity recipient;

    @BeforeEach
    void setUp() {
        actor = user(1L, "actor@test.com", "Actor");
        recipient = user(2L, "recipient@test.com", "Recipient");
    }

    @Test
    void shouldGetNotificationsForCurrentUserAndMapPage() {
        NotificationEntity entity = notification(
                10L,
                recipient,
                actor,
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                50L,
                "Actor accepted your trade."
        );

        NotificationResponse response = NotificationResponse.builder()
                .id(10L)
                .type("TRADE_ACCEPTED")
                .resourceType("TRADE")
                .resourceId(50L)
                .actorId(1L)
                .actorName("Actor")
                .message("Actor accepted your trade.")
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);
        when(notificationRepo.findByRecipient_UserIdOrderByCreatedAtDesc(
                eq(2L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(notificationMapper.map(entity)).thenReturn(response);

        Page<NotificationResponse> result =
                notificationService.getNotifications(0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals(response, result.getContent().get(0));

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(notificationRepo).findByRecipient_UserIdOrderByCreatedAtDesc(
                eq(2L), pageableCaptor.capture());

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(20, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void shouldCapNotificationPageSizeAt100() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);
        when(notificationRepo.findByRecipient_UserIdOrderByCreatedAtDesc(
                eq(2L), any(Pageable.class)))
                .thenReturn(Page.empty());

        notificationService.getNotifications(2, 500);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(notificationRepo).findByRecipient_UserIdOrderByCreatedAtDesc(
                eq(2L), pageableCaptor.capture());

        assertEquals(2, pageableCaptor.getValue().getPageNumber());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void shouldGetNotificationCounts() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);
        when(notificationRepo.countByRecipient_UserIdAndIsReadFalse(2L))
                .thenReturn(7L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.TRADE))
                .thenReturn(4L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.CHAT_CONVERSATION))
                .thenReturn(3L);

        NotificationCountResponse result =
                notificationService.getNotificationCounts();

        assertEquals(7L, result.getUnreadCount());
        assertEquals(4L, result.getTradeAlertCount());
        assertEquals(3L, result.getChatAlertCount());
    }

    @Test
    void shouldGetUnreadCount() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);
        when(notificationRepo.countByRecipient_UserIdAndIsReadFalse(2L))
                .thenReturn(5L);

        Long result = notificationService.getUnreadCount();

        assertEquals(5L, result);
    }

    @Test
    void shouldMarkNotificationReadForCurrentUser() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);

        notificationService.markRead(10L);

        verify(notificationRepo).markRead(10L, 2L);
    }

    @Test
    void shouldMarkModuleReadForCurrentUser() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);

        notificationService.markModuleRead(
                NotificationResourceType.CHAT_CONVERSATION
        );

        verify(notificationRepo).markModuleRead(
                2L, NotificationResourceType.CHAT_CONVERSATION
        );
    }

    @Test
    void shouldDeleteOwnedNotification() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);

        notificationService.delete(10L);

        verify(notificationRepo).deleteOwned(10L, 2L);
    }

    @Test
    void shouldMarkAllNotificationsRead() {
        when(authenticatedUserService.getCurrentUser()).thenReturn(recipient);

        notificationService.markAllRead();

        verify(notificationRepo).markAllRead(2L);
    }

    @Test
    void shouldCreateTradeAcceptedNotificationAndSendWebsocketUpdate() {
        when(userPreferenceService.isNotificationEnabled(recipient.getUserId(), NotificationType.TRADE_ACCEPTED)).thenReturn(true);
        NotificationEntity saved = NotificationEntity.builder()
                .id(10L)
                .recipient(recipient)
                .actor(actor)
                .type(NotificationType.TRADE_ACCEPTED)
                .resourceType(NotificationResourceType.TRADE)
                .resourceId(50L)
                .message("Actor accepted your trade.")
                .isRead(false)
                .build();

        NotificationResponse dto = NotificationResponse.builder()
                .id(10L)
                .type("TRADE_ACCEPTED")
                .resourceType("TRADE")
                .resourceId(50L)
                .actorId(1L)
                .actorName("Actor")
                .message("Actor accepted your trade.")
                .read(false)
                .build();

        when(notificationRepo.save(any(NotificationEntity.class)))
                .thenReturn(saved);
        when(notificationMapper.map(saved)).thenReturn(dto);
        when(notificationRepo.countByRecipient_UserIdAndIsReadFalse(2L))
                .thenReturn(6L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.TRADE))
                .thenReturn(4L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.CHAT_CONVERSATION))
                .thenReturn(2L);

        notificationService.notifyTradeAccepted(actor, recipient, 50L);

        ArgumentCaptor<NotificationEntity> captor =
                ArgumentCaptor.forClass(NotificationEntity.class);

        verify(notificationRepo).save(captor.capture());

        NotificationEntity created = captor.getValue();
        assertSame(recipient, created.getRecipient());
        assertSame(actor, created.getActor());
        assertEquals(NotificationType.TRADE_ACCEPTED, created.getType());
        assertEquals(NotificationResourceType.TRADE, created.getResourceType());
        assertEquals(50L, created.getResourceId());
        assertEquals("Actor accepted your trade.", created.getMessage());
        assertFalse(created.isRead());

        ArgumentCaptor<NotificationWebsocketResponse> websocketCaptor =
                ArgumentCaptor.forClass(NotificationWebsocketResponse.class);

        verify(messagingTemplate).convertAndSendToUser(
                eq(recipient.getEmail()),
                eq("/queue/notifications"),
                websocketCaptor.capture()
        );

        NotificationWebsocketResponse websocket = websocketCaptor.getValue();
        assertSame(dto, websocket.getNotification());
        assertEquals(6L, websocket.getUnreadCount());
        assertEquals(4L, websocket.getTradeAlertCount());
        assertEquals(2L, websocket.getChatAlertCount());
    }

    @Test
    void shouldCreateCounterTradeOfferMessage() {
        when(userPreferenceService.isNotificationEnabled(recipient.getUserId(), NotificationType.TRADE_OFFER_RECEIVED))
                .thenReturn(true);
        when(notificationRepo.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationMapper.map(any(NotificationEntity.class)))
                .thenReturn(NotificationResponse.builder().build());
        when(notificationRepo.countByRecipient_UserIdAndIsReadFalse(2L))
                .thenReturn(1L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.TRADE))
                .thenReturn(1L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.CHAT_CONVERSATION))
                .thenReturn(0L);

        notificationService.notifyTradeOfferReceived(
                actor, recipient, 99L, true
        );

        ArgumentCaptor<NotificationEntity> captor =
                ArgumentCaptor.forClass(NotificationEntity.class);

        verify(notificationRepo).save(captor.capture());

        assertEquals(
                "Actor countered your trade offer.",
                captor.getValue().getMessage()
        );
        assertEquals(
                NotificationType.TRADE_OFFER_RECEIVED,
                captor.getValue().getType()
        );
    }

    @Test
    void shouldNotCreateNotificationWhenActorIsRecipient() {
        notificationService.notifyTradeAccepted(
                recipient, recipient, 50L
        );

        verifyNoInteractions(notificationRepo, notificationMapper, messagingTemplate);
    }

    @Test
    void shouldNotCreateChatNotificationWhenRecipientIsViewingConversation() {
        when(activeConversationRegistry.isViewingConversation(2L, 500L))
                .thenReturn(true);

        notificationService.notifyChatMessage(
                actor, recipient, 500L
        );

        verifyNoInteractions(notificationRepo, notificationMapper, messagingTemplate);
    }

    @Test
    void shouldCreateChatNotificationWhenRecipientIsNotViewingConversation() {
        when(userPreferenceService.isNotificationEnabled(recipient.getUserId(), NotificationType.CHAT_MESSAGE))
                .thenReturn(true);
        when(activeConversationRegistry.isViewingConversation(2L, 500L))
                .thenReturn(false);
        when(notificationRepo.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationMapper.map(any(NotificationEntity.class)))
                .thenReturn(NotificationResponse.builder().build());
        when(notificationRepo.countByRecipient_UserIdAndIsReadFalse(2L))
                .thenReturn(2L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.TRADE))
                .thenReturn(0L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.CHAT_CONVERSATION))
                .thenReturn(2L);

        notificationService.notifyChatMessage(
                actor, recipient, 500L
        );

        ArgumentCaptor<NotificationEntity> captor =
                ArgumentCaptor.forClass(NotificationEntity.class);

        verify(notificationRepo).save(captor.capture());

        NotificationEntity created = captor.getValue();
        assertEquals(NotificationType.CHAT_MESSAGE, created.getType());
        assertEquals(
                NotificationResourceType.CHAT_CONVERSATION,
                created.getResourceType()
        );
        assertEquals(500L, created.getResourceId());
        assertEquals("Actor sent you a message.", created.getMessage());
    }

    @Test
    void shouldCreateSystemNotificationWithoutActor() {
        when(userPreferenceService.isNotificationEnabled(recipient.getUserId(), NotificationType.SYSTEM))
                .thenReturn(true);
        when(notificationRepo.save(any(NotificationEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationMapper.map(any(NotificationEntity.class)))
                .thenReturn(NotificationResponse.builder().build());
        when(notificationRepo.countByRecipient_UserIdAndIsReadFalse(2L))
                .thenReturn(3L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.TRADE))
                .thenReturn(1L);
        when(notificationRepo.countUnreadByResourceType(
                2L, NotificationResourceType.CHAT_CONVERSATION))
                .thenReturn(1L);

        notificationService.notifySystem(
                recipient, "System maintenance tonight."
        );

        ArgumentCaptor<NotificationEntity> captor =
                ArgumentCaptor.forClass(NotificationEntity.class);

        verify(notificationRepo).save(captor.capture());

        NotificationEntity created = captor.getValue();
        assertSame(recipient, created.getRecipient());
        assertNull(created.getActor());
        assertEquals(NotificationType.SYSTEM, created.getType());
        assertEquals(NotificationResourceType.SYSTEM, created.getResourceType());
        assertEquals(recipient.getUserId(), created.getResourceId());
        assertEquals("System maintenance tonight.", created.getMessage());
    }

    private NotificationEntity notification(
            Long id,
            UserEntity recipient,
            UserEntity actor,
            NotificationType type,
            NotificationResourceType resourceType,
            Long resourceId,
            String message
    ) {
        return NotificationEntity.builder()
                .id(id)
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UserEntity user(Long id, String email, String name) {
        return UserEntity.builder()
                .userId(id)
                .email(email)
                .name(name)
                .password("password")
                .build();
    }
}
