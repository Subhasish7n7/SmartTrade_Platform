package smarttrade.backend.entities.trade;

public enum DisputeResolution {
    MARK_COMPLETED,
    CANCEL_NO_FAULT,
    CANCEL_WITH_FAULT,
    REOPEN_TRADE
}