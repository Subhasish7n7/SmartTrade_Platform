package smarttrade.backend.entities.notification;

public enum NotificationType {

    TRADE_OFFER_CREATED,
    TRADE_OFFER_RECEIVED,
    TRADE_ACCEPTED,

    TRADE_COMPLETION_REQUESTED,
    TRADE_COMPLETED,
    TRADE_CANCELLED,
    TRADE_EXPIRED,

    BUY_OFFER_RECEIVED,

    CHAT_MESSAGE,

    SYSTEM
}