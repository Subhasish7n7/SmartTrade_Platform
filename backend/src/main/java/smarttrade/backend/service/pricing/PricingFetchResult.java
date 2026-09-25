package smarttrade.backend.service.pricing;

public record PricingFetchResult(
        PricingFetchStatus status,
        Integer price,
        String errorCode,
        String errorMessage,
        Integer httpStatus
) {

    public static PricingFetchResult success(Integer price) {
        return new PricingFetchResult(
                PricingFetchStatus.SUCCESS, price, null, null, 200
        );
    }

    public static PricingFetchResult failure(
            PricingFetchStatus status,
            String errorCode,
            String errorMessage,
            Integer httpStatus
    ) {
        return new PricingFetchResult(
                status, null, errorCode, errorMessage, httpStatus
        );
    }
}