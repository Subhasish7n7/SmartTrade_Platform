package smarttrade.backend.UnitTest.Pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.events.ItemPersistedEvent;
import smarttrade.backend.service.pricing.PricingBackgroundService;
import smarttrade.backend.events.ItemPricingListener;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemPricingListenerTest {

    @Mock
    private PricingBackgroundService pricingBackgroundService;

    @InjectMocks
    private ItemPricingListener itemPricingListener;

    @Test
    void shouldRequestBackgroundPricingForPersistedItem() {
        ItemPersistedEvent event = new ItemPersistedEvent(42L);

        itemPricingListener.handle(event);

        verify(pricingBackgroundService).requestAndExecute(42L);
    }
}
