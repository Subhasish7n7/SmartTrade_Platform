package smarttrade.backend.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import smarttrade.backend.service.pricing.PricingBackgroundService;

@Component
@RequiredArgsConstructor
public class ItemPricingListener {

    private final PricingBackgroundService pricingBackgroundService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ItemPersistedEvent event) {
        pricingBackgroundService.requestAndExecute(event.itemId());
    }
}