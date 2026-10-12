package smarttrade.backend.entities.trade;

public enum TradeStatus {
    OPEN,
    NEGOTIATING,
    ACCEPTED,
    DISPUTED,
    COMPLETED,
    CANCELLED,
    EXPIRED
}