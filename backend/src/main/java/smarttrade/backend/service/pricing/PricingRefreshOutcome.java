package smarttrade.backend.service.pricing;

public enum PricingRefreshOutcome {
    STARTED,
    ALREADY_RUNNING,
    ALREADY_FRESH,
    NOT_FOUND
}