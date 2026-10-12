package smarttrade.backend.service.pricing;

public enum PricingFetchStatus {
    SUCCESS,
    NO_RESULTS,
    NO_RELEVANT_CANDIDATES,
    INVALID_CONFIGURATION,
    AUTHENTICATION_FAILED,
    RATE_LIMITED,
    BAD_REQUEST,
    PROVIDER_ERROR,
    NETWORK_ERROR
}
