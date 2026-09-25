package smarttrade.backend.service.pricing;

import smarttrade.backend.entities.item.ItemPricingStatus;

import java.time.LocalDateTime;

public record PricingRefreshResponse(
        Long itemId,
        PricingRefreshOutcome outcome,
        ItemPricingStatus pricingStatus,
        long pricingVersion,
        Integer userPrice,
        Integer newPrice,
        Integer generatedPrice,
        LocalDateTime priceFetchedAt,
        LocalDateTime nextPriceRefreshAt,
        String pricingErrorCode,
        String pricingErrorMessage,
        String message
) {
}