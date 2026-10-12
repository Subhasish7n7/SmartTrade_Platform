package smarttrade.backend.UnitTest.Pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemPricingStatus;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.service.pricing.PricingJobDecision;
import smarttrade.backend.service.pricing.PricingJobRequest;
import smarttrade.backend.service.pricing.PricingJobService;
import smarttrade.backend.service.pricing.PricingRefreshOutcome;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingJobServiceTest {

    @Mock
    private ItemRepo itemRepo;

    @InjectMocks
    private PricingJobService pricingJobService;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(
                pricingJobService,
                "refreshWindowHours",
                168L
        );
    }

    @Test
    void shouldReturnNotFoundWhenItemDoesNotExist() {
        when(itemRepo.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        PricingJobDecision decision =
                pricingJobService.requestRefresh(99L);

        assertEquals(PricingRefreshOutcome.NOT_FOUND, decision.response().outcome());
        assertNull(decision.job());

        verify(itemRepo).findByIdForUpdate(99L);
        verifyNoMoreInteractions(itemRepo);
    }

    @Test
    void shouldStartNewPricingJobAndInitializeMissingVersion() {
        ItemEntity item = baseItem();
        item.setPricingVersion(null);
        item.setPricingStatus(ItemPricingStatus.PENDING);
        item.setLabels(List.of("gaming", "laptop"));

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item)).thenReturn(item);

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(PricingRefreshOutcome.STARTED, decision.response().outcome());
        assertEquals(ItemPricingStatus.IN_PROGRESS, item.getPricingStatus());
        assertEquals(1L, item.getPricingVersion());
        assertNull(item.getPricingErrorCode());
        assertNull(item.getPricingErrorMessage());

        PricingJobRequest job = decision.job();
        assertNotNull(job);
        assertEquals(1L, job.itemId());
        assertEquals(1L, job.pricingVersion());
        assertEquals("Laptop", job.itemName());
        assertEquals(ItemCategory.COMPUTERS, job.category());
        assertEquals(List.of("gaming", "laptop"), job.labels());

        verify(itemRepo).findByIdForUpdate(1L);
        verify(itemRepo).save(item);
    }

    @Test
    void shouldStartPricingForPendingCurrentVersion() {
        ItemEntity item = baseItem();
        item.setPricingVersion(4L);
        item.setPricingStatus(ItemPricingStatus.PENDING);
        item.setPricingErrorCode("OLD_ERROR");
        item.setPricingErrorMessage("old");

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item)).thenReturn(item);

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(PricingRefreshOutcome.STARTED, decision.response().outcome());
        assertEquals(4L, decision.response().pricingVersion());
        assertEquals(4L, decision.job().pricingVersion());
        assertEquals(ItemPricingStatus.IN_PROGRESS, item.getPricingStatus());
        assertNull(item.getPricingErrorCode());
        assertNull(item.getPricingErrorMessage());

        verify(itemRepo).save(item);
    }

    @Test
    void shouldReturnAlreadyRunningWithoutCreatingJob() {
        ItemEntity item = baseItem();
        item.setPricingVersion(3L);
        item.setPricingStatus(ItemPricingStatus.IN_PROGRESS);

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(
                PricingRefreshOutcome.ALREADY_RUNNING,
                decision.response().outcome()
        );
        assertEquals(ItemPricingStatus.IN_PROGRESS, decision.response().pricingStatus());
        assertNull(decision.job());

        verify(itemRepo).findByIdForUpdate(1L);
        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldReturnAlreadyFreshWhenPriceIsStillFresh() {
        ItemEntity item = baseItem();
        item.setPricingVersion(2L);
        item.setPricingStatus(ItemPricingStatus.READY);
        item.setPriceFetchedAt(LocalDateTime.now().minusHours(2));

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(
                PricingRefreshOutcome.ALREADY_FRESH,
                decision.response().outcome()
        );
        assertEquals(ItemPricingStatus.READY, decision.response().pricingStatus());
        assertNull(decision.job());
        assertNotNull(decision.response().nextPriceRefreshAt());

        verify(itemRepo).findByIdForUpdate(1L);
        verify(itemRepo, never()).save(any());
    }

    @Test
    void shouldStartWhenPriceIsExpired() {
        ItemEntity item = baseItem();
        item.setPricingVersion(2L);
        item.setPricingStatus(ItemPricingStatus.READY);
        item.setPriceFetchedAt(LocalDateTime.now().minusHours(169));

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item)).thenReturn(item);

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(PricingRefreshOutcome.STARTED, decision.response().outcome());
        assertEquals(ItemPricingStatus.IN_PROGRESS, item.getPricingStatus());
        assertNotNull(decision.job());
        assertEquals(2L, decision.job().pricingVersion());
    }

    @Test
    void shouldTreatNullPricingStatusAsPending() {
        ItemEntity item = baseItem();
        item.setPricingVersion(5L);
        item.setPricingStatus(null);

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item)).thenReturn(item);

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(PricingRefreshOutcome.STARTED, decision.response().outcome());
        assertEquals(ItemPricingStatus.IN_PROGRESS, item.getPricingStatus());
        assertEquals(5L, item.getPricingVersion());
    }

    @Test
    void shouldNotShareMutableLabelsWithItem() {
        ItemEntity item = baseItem();
        item.setLabels(new java.util.ArrayList<>(List.of("phone")));

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item)).thenReturn(item);

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(List.of("phone"), decision.job().labels());
        assertNotSame(item.getLabels(), decision.job().labels());
    }

    @Test
    void shouldUseEmptyLabelsWhenItemLabelsAreNull() {
        ItemEntity item = baseItem();
        item.setLabels(null);

        when(itemRepo.findByIdForUpdate(1L))
                .thenReturn(Optional.of(item));
        when(itemRepo.save(item)).thenReturn(item);

        PricingJobDecision decision =
                pricingJobService.requestRefresh(1L);

        assertEquals(List.of(), decision.job().labels());
    }

    @Test
    void shouldReturnFalseForFreshnessWhenNotReady() {
        ItemEntity item = baseItem();

        item.setPricingStatus(ItemPricingStatus.PENDING);
        item.setPriceFetchedAt(LocalDateTime.now());

        assertFalse(pricingJobService.isPriceFresh(item));

        item.setPricingStatus(ItemPricingStatus.READY);
        item.setPriceFetchedAt(null);

        assertFalse(pricingJobService.isPriceFresh(item));
    }

    @Test
    void shouldCalculateNextRefreshAt() {
        ItemEntity item = baseItem();

        LocalDateTime fetchedAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        item.setPriceFetchedAt(fetchedAt);

        assertEquals(
                fetchedAt.plusHours(168),
                pricingJobService.nextPriceRefreshAt(item)
        );
    }

    @Test
    void shouldReturnNullForNextRefreshWithoutFetchedAt() {
        ItemEntity item = baseItem();
        item.setPriceFetchedAt(null);

        assertNull(pricingJobService.nextPriceRefreshAt(item));
    }

    private ItemEntity baseItem() {
        return ItemEntity.builder()
                .itemId(1L)
                .itemName("Laptop")
                .category(ItemCategory.COMPUTERS)
                .condition(ItemCondition.GOOD)
                .customCategory(null)
                .labels(List.of("gaming"))
                .userPrice(50000)
                .newPrice(80000)
                .generatedPrice(56000)
                .pricingVersion(1L)
                .pricingStatus(ItemPricingStatus.PENDING)
                .forSale(true)
                .available(true)
                .build();
    }
}
