package smarttrade.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttrade.backend.Mappers.NotificationMapper;
import smarttrade.backend.dto.notification.NotificationResponse;
import smarttrade.backend.dto.notification.NotificationWebsocketResponse;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.entities.notification.NotificationEntity;
import smarttrade.backend.entities.notification.NotificationResourceType;
import smarttrade.backend.entities.notification.NotificationType;
import smarttrade.backend.repository.NotificationRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthenticatedUserService authenticatedUserService;
    private final ActiveConversationRegistry activeConversationRegistry;

    /*
     ----------------------------------------------------------
     PUBLIC API
     ----------------------------------------------------------
     */

    public Page<NotificationResponse> getNotifications(int page, int size) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        size = Math.min(size,100);

        return notificationRepo
                .findByRecipient_UserIdOrderByCreatedAtDesc(
                        currentUser.getUserId(),
                        PageRequest.of(page, size)
                )
                .map(notificationMapper::map);
    }

    public Long getUnreadCount() {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        return notificationRepo.countByRecipient_UserIdAndIsReadFalse(currentUser.getUserId());
    }

    public void markRead(Long id) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        NotificationEntity notification =  notificationRepo.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"));

        if (!notification.getRecipient().getUserId()
                .equals(currentUser.getUserId())) {

            throw new IllegalStateException(
                    "Access denied");
        }

        if (notification.isRead()) {
            return;
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        notificationRepo.save(notification);
    }
    public void delete(Long id) {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        NotificationEntity notification =
                notificationRepo.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"));

        if (!notification.getRecipient().getUserId()
                .equals(currentUser.getUserId())) {

            throw new IllegalStateException(
                    "Access denied");
        }

        notificationRepo.delete(notification);
    }

    public void markAllRead() {

        UserEntity currentUser = authenticatedUserService.getCurrentUser();

        notificationRepo.markAllRead(currentUser.getUserId());
    }

    /*
     ----------------------------------------------------------
     TRADE
     ----------------------------------------------------------
     */

    public void notifyTradeOfferReceived(UserEntity actor, UserEntity receiver, Long tradeId, boolean counter) {


        createNotification(
                receiver,
                actor,
                NotificationType.TRADE_OFFER_RECEIVED,
                NotificationResourceType.TRADE,
                tradeId,
                counter
                        ? actor.getName() + " countered your trade offer."
                        : actor.getName() + " sent you a trade offer."

        );
    }

    public void notifyTradeAccepted( UserEntity actor, UserEntity receiver, Long tradeId) {

        createNotification(
                receiver,
                actor,
                NotificationType.TRADE_ACCEPTED,
                NotificationResourceType.TRADE,
                tradeId,
                actor.getName() + " accepted your trade."
        );
    }

    public void notifyTradeCancelled(UserEntity actor, UserEntity receiver, Long tradeId) {

        createNotification(
                receiver,
                actor,
                NotificationType.TRADE_CANCELLED,
                NotificationResourceType.TRADE,
                tradeId,
                actor.getName() + " cancelled the trade."
        );
    }
    public void notifyTradeExpired(UserEntity recipient, UserEntity actor, Long tradeId) {

        createNotification(
                recipient,
                actor,
                NotificationType.TRADE_EXPIRED,
                NotificationResourceType.TRADE,
                tradeId,
                "Trade expired due to inactivity."
        );
    }

    public void notifyCompletionRequested(UserEntity actor, UserEntity receiver, Long tradeId) {

        createNotification(
                receiver,
                actor,
                NotificationType.TRADE_COMPLETION_REQUESTED,
                NotificationResourceType.TRADE,
                tradeId,
                actor.getName() + " requested trade completion."
        );
    }

    public void notifyTradeCompleted(UserEntity actor, UserEntity receiver, Long tradeId) {

        createNotification(
                receiver,
                actor,
                NotificationType.TRADE_COMPLETED,
                NotificationResourceType.TRADE,
                tradeId,
                "Trade completed successfully."
        );
    }

    /*
     ----------------------------------------------------------
     BUY OFFER
     ----------------------------------------------------------
     */

    public void notifyBuyOffer(UserEntity buyer, UserEntity seller, Long tradeId) {

        createNotification(
                seller,
                buyer,
                NotificationType.BUY_OFFER_RECEIVED,
                NotificationResourceType.TRADE,
                tradeId,
                buyer.getName() + " sent you a buy offer."
        );
    }

    /*
     ----------------------------------------------------------
     CHAT
     ----------------------------------------------------------
     */

    public void notifyChatMessage(UserEntity sender, UserEntity receiver, Long conversationId) {

        if (activeConversationRegistry.isViewingConversation(receiver.getUserId(), conversationId)) {
            return;
        }

        createNotification(
                receiver,
                sender,
                NotificationType.CHAT_MESSAGE,
                NotificationResourceType.CHAT_CONVERSATION,
                conversationId,
                sender.getName() + " sent you a message."
        );
    }

    /*
     ----------------------------------------------------------
     SYSTEM
     ----------------------------------------------------------
     */

    public void notifySystem(UserEntity receiver, String message) {

        createNotification(
                receiver,
                null,
                NotificationType.SYSTEM,
                NotificationResourceType.SYSTEM,
                receiver.getUserId(),
                message
        );
    }

    /*
     ----------------------------------------------------------
     INTERNAL
     ----------------------------------------------------------
     */

    private void createNotification(UserEntity recipient, UserEntity actor,
            NotificationType type, NotificationResourceType resourceType, Long resourceId, String message) {

        if (actor != null && actor.getUserId().equals(recipient.getUserId())) {
            return;
        }

        NotificationEntity notification =
                NotificationEntity.builder()
                        .recipient(recipient)
                        .actor(actor)
                        .type(type)
                        .resourceType(resourceType)
                        .resourceId(resourceId)
                        .message(message)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        notification = notificationRepo.save(notification);

        NotificationResponse dto = notificationMapper.map(notification);

        Long unread = notificationRepo.countByRecipient_UserIdAndIsReadFalse(recipient.getUserId());

        NotificationWebsocketResponse websocket =
                NotificationWebsocketResponse.builder()
                        .notification(dto)
                        .unreadCount(unread)
                        .build();

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                websocket
        );
    }

}