package smarttrade.backend.events.itemPriceFetch;

import java.time.LocalDateTime;

public record ItemPriceUpdatedEvent(
        Long itemId,
        Integer newPrice,
        Integer generatedPrice,
        LocalDateTime priceFetchedAt
) {
}