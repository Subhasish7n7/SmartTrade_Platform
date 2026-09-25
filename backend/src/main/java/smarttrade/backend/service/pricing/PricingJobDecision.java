package smarttrade.backend.service.pricing;

import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemPricingStatus;

import java.time.LocalDateTime;

public record PricingJobDecision(
        PricingRefreshResponse response,
        PricingJobRequest job
) {

    public static PricingJobDecision started(
            ItemEntity item,
            long version,
            LocalDateTime nextPriceRefreshAt,
            PricingJobRequest job
    ) {
        return new PricingJobDecision(
                new PricingRefreshResponse(
                        item.getItemId(),
                        PricingRefreshOutcome.STARTED,
                        ItemPricingStatus.IN_PROGRESS,
                        version,
                        item.getUserPrice(),
                        item.getNewPrice(),
                        item.getGeneratedPrice(),
                        item.getPriceFetchedAt(),
                        nextPriceRefreshAt,
                        item.getPricingErrorCode(),
                        item.getPricingErrorMessage(),
                        "Market pricing update started."
                ),
                job
        );
    }

    public static PricingJobDecision alreadyRunning(
            ItemEntity item,
            long version,
            LocalDateTime nextPriceRefreshAt
    ) {
        return new PricingJobDecision(
                new PricingRefreshResponse(
                        item.getItemId(),
                        PricingRefreshOutcome.ALREADY_RUNNING,
                        ItemPricingStatus.IN_PROGRESS,
                        version,
                        item.getUserPrice(),
                        item.getNewPrice(),
                        item.getGeneratedPrice(),
                        item.getPriceFetchedAt(),
                        nextPriceRefreshAt,
                        item.getPricingErrorCode(),
                        item.getPricingErrorMessage(),
                        "A market pricing update is already running."
                ),
                null
        );
    }

    public static PricingJobDecision alreadyFresh(
            ItemEntity item,
            long version,
            LocalDateTime nextPriceRefreshAt
    ) {
        return new PricingJobDecision(
                new PricingRefreshResponse(
                        item.getItemId(),
                        PricingRefreshOutcome.ALREADY_FRESH,
                        ItemPricingStatus.READY,
                        version,
                        item.getUserPrice(),
                        item.getNewPrice(),
                        item.getGeneratedPrice(),
                        item.getPriceFetchedAt(),
                        nextPriceRefreshAt,
                        item.getPricingErrorCode(),
                        item.getPricingErrorMessage(),
                        "Market pricing is already up to date."
                ),
                null
        );
    }

    public static PricingJobDecision notFound(Long itemId) {
        return new PricingJobDecision(
                new PricingRefreshResponse(
                        itemId,
                        PricingRefreshOutcome.NOT_FOUND,
                        null,
                        0L,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Item not found."
                ),
                null
        );
    }
}