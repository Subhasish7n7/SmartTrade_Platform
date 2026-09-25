package smarttrade.backend.UnitTest.Pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemPricingStatus;
import smarttrade.backend.events.ItemPriceUpdatedEvent;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.service.pricing.ItemPricingService;
import smarttrade.backend.service.pricing.PriceEstimationService;
import smarttrade.backend.service.pricing.PricingFetchResult;
import smarttrade.backend.service.pricing.PricingFetchStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemPricingServiceTest {

    @Mock
    private ItemRepo itemRepo;

    @Mock
    private PriceEstimationService priceEstimationService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ItemPricingService itemPricingService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.reset(
                itemRepo,
                priceEstimationService,
                applicationEventPublisher
        );
    }

    @Test
    void shouldIgnoreMissingItemOnCompletePricing() {
        when(itemRepo.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        itemPricingService.completePricing(
                99L,
                1L,
                PricingFetchResult.success(1000)
        );

        verify(itemRepo).findByIdForUpdate(99L);
        verifyNoMoreInteractions(itemRepo);
        verifyNoInteractions(priceEstimationService, applicationEventPublisher);
    }

    @Test
    void shouldIgnoreStalePricingVersion() {
        ItemEntity item = pricingInProgressItem();
        item.setPricingVersion(5L);

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));

        itemPricingService.completePricing(
                1L,
                4L,
                PricingFetchResult.success(100000)
        );

        assertEquals(5L, item.getPricingVersion());
        assertEquals(ItemPricingStatus.IN_PROGRESS, item.getPricingStatus());
        verify(itemRepo).findByIdForUpdate(1L);
        verify(itemRepo, never()).save(any());
        verifyNoInteractions(priceEstimationService, applicationEventPublisher);
    }

    @Test
    void shouldIgnoreResultWhenPricingIsNotInProgress() {
        ItemEntity item = pricingInProgressItem();
        item.setPricingStatus(ItemPricingStatus.PENDING);

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));

        itemPricingService.completePricing(
                1L,
                1L,
                PricingFetchResult.success(100000)
        );

        verify(itemRepo).findByIdForUpdate(1L);
        verify(itemRepo, never()).save(any());
        verifyNoInteractions(priceEstimationService, applicationEventPublisher);
    }

    @Test
    void shouldFailWhenPricingResultIsNull() {
        ItemEntity item = pricingInProgressItem();

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item))
                .thenReturn(item);

        itemPricingService.completePricing(1L, 1L, null);

        assertEquals(ItemPricingStatus.FAILED, item.getPricingStatus());
        assertEquals("NULL_PRICING_RESULT", item.getPricingErrorCode());
        assertEquals(
                "The pricing provider returned no usable result.",
                item.getPricingErrorMessage()
        );

        verify(itemRepo).save(item);
        verifyNoInteractions(priceEstimationService, applicationEventPublisher);
    }

    @Test
    void shouldMarkFailureUsingProviderErrorCodeAndMessage() {
        ItemEntity item = pricingInProgressItem();

        PricingFetchResult failure =
                PricingFetchResult.failure(
                        PricingFetchStatus.NO_RESULTS,
                        "NO_USABLE_PRICES",
                        "No prices found.",
                        200
                );

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item))
                .thenReturn(item);

        itemPricingService.completePricing(
                1L,
                1L,
                failure
        );

        assertEquals(ItemPricingStatus.FAILED, item.getPricingStatus());
        assertEquals("NO_USABLE_PRICES", item.getPricingErrorCode());
        assertEquals("No prices found.", item.getPricingErrorMessage());

        verify(itemRepo).save(item);
        verifyNoInteractions(priceEstimationService, applicationEventPublisher);
    }

    @Test
    void shouldFallbackToStatusAndDefaultMessageWhenProviderDetailsAreMissing() {
        ItemEntity item = pricingInProgressItem();

        PricingFetchResult failure =
                PricingFetchResult.failure(
                        PricingFetchStatus.PROVIDER_ERROR,
                        null,
                        null,
                        500
                );

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item))
                .thenReturn(item);

        itemPricingService.completePricing(
                1L,
                1L,
                failure
        );

        assertEquals(ItemPricingStatus.FAILED, item.getPricingStatus());
        assertEquals("PROVIDER_ERROR", item.getPricingErrorCode());
        assertEquals(
                "Market pricing could not be updated.",
                item.getPricingErrorMessage()
        );
    }

    @Test
    void shouldCompletePricingGenerateUsedPriceSaveAndPublishEvent() {
        ItemEntity item = pricingInProgressItem();
        item.setPricingErrorCode("OLD");
        item.setPricingErrorMessage("old error");

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item))
                .thenReturn(item);
        when(priceEstimationService.generateUsedPrice(
                100000,
                ItemCondition.GOOD
        )).thenReturn(70000);

        LocalDateTime before = LocalDateTime.now();

        itemPricingService.completePricing(
                1L,
                1L,
                PricingFetchResult.success(100000)
        );

        LocalDateTime after = LocalDateTime.now();

        assertEquals(100000, item.getNewPrice());
        assertEquals(70000, item.getGeneratedPrice());
        assertEquals(ItemPricingStatus.READY, item.getPricingStatus());
        assertNull(item.getPricingErrorCode());
        assertNull(item.getPricingErrorMessage());
        assertNotNull(item.getPriceFetchedAt());

        assertFalse(item.getPriceFetchedAt().isBefore(before));
        assertFalse(item.getPriceFetchedAt().isAfter(after));

        verify(priceEstimationService).generateUsedPrice(
                100000,
                ItemCondition.GOOD
        );
        verify(itemRepo).save(item);

        ArgumentCaptor<ItemPriceUpdatedEvent> eventCaptor =
                ArgumentCaptor.forClass(ItemPriceUpdatedEvent.class);

        verify(applicationEventPublisher)
                .publishEvent(eventCaptor.capture());

        ItemPriceUpdatedEvent event = eventCaptor.getValue();

        assertEquals(1L, event.itemId());
        assertEquals(100000, event.newPrice());
        assertEquals(70000, event.generatedPrice());
        assertEquals(item.getPriceFetchedAt(), event.priceFetchedAt());
    }

    @Test
    void shouldIgnoreMissingItemOnFailPricing() {
        when(itemRepo.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        itemPricingService.failPricing(
                99L,
                1L,
                "ERROR",
                "message"
        );

        verify(itemRepo).findByIdForUpdate(99L);
        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldIgnoreStaleVersionOnFailPricing() {
        ItemEntity item = pricingInProgressItem();
        item.setPricingVersion(2L);

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));

        itemPricingService.failPricing(
                1L,
                1L,
                "ERROR",
                "message"
        );

        verify(itemRepo).findByIdForUpdate(1L);
        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldIgnoreFailPricingWhenStatusIsNotInProgress() {
        ItemEntity item = pricingInProgressItem();
        item.setPricingStatus(ItemPricingStatus.READY);

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));

        itemPricingService.failPricing(
                1L,
                1L,
                "ERROR",
                "message"
        );

        verify(itemRepo).findByIdForUpdate(1L);
        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldMarkPricingFailed() {
        ItemEntity item = pricingInProgressItem();

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item))
                .thenReturn(item);

        itemPricingService.failPricing(
                1L,
                1L,
                "NETWORK_ERROR",
                "provider unavailable"
        );

        assertEquals(ItemPricingStatus.FAILED, item.getPricingStatus());
        assertEquals("NETWORK_ERROR", item.getPricingErrorCode());
        assertEquals("provider unavailable", item.getPricingErrorMessage());

        verify(itemRepo).save(item);
    }

    private ItemEntity pricingInProgressItem() {
        return ItemEntity.builder()
                .itemId(1L)
                .itemName("Laptop")
                .category(ItemCategory.COMPUTERS)
                .condition(ItemCondition.GOOD)
                .userPrice(50000)
                .newPrice(80000)
                .generatedPrice(56000)
                .pricingVersion(1L)
                .pricingStatus(ItemPricingStatus.IN_PROGRESS)
                .build();
    }
}
