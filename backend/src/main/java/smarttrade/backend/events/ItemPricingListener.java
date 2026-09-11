package smarttrade.backend.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import smarttrade.backend.service.ItemPricingService;
import smarttrade.backend.service.PriceEstimationService;

@Component
@RequiredArgsConstructor
public class ItemPricingListener {

    private final PriceEstimationService priceEstimationService;
    private final ItemPricingService itemPricingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ItemPersistedEvent event) {

        Integer newPrice =
                priceEstimationService.fetchEstimatedNewPrice(
                        event.itemName(),
                        event.category(),
                        event.customCategory(),
                        event.labels()
                );

        if (newPrice == null) {
            // The item is already persisted. Pricing failure must not
            // roll back or invalidate the listing.
            return;
        }

        itemPricingService.updatePrices(event.itemId(), newPrice);
    }
}