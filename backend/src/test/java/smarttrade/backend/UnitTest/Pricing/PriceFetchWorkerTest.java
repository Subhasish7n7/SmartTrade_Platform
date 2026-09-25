package smarttrade.backend.UnitTest.Pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.service.pricing.ItemPricingService;
import smarttrade.backend.service.pricing.PriceEstimationService;
import smarttrade.backend.service.pricing.PriceFetchWorker;
import smarttrade.backend.service.pricing.PricingFetchResult;
import smarttrade.backend.service.pricing.PricingJobRequest;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceFetchWorkerTest {

    @Mock
    private PriceEstimationService priceEstimationService;

    @Mock
    private ItemPricingService itemPricingService;

    @InjectMocks
    private PriceFetchWorker priceFetchWorker;

    private PricingJobRequest job;

    @BeforeEach
    void setUp() {
        job = new PricingJobRequest(
                1L,
                4L,
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                List.of("gaming", "16gb")
        );
    }

    @Test
    void shouldFetchPriceAndCompletePricing() {
        PricingFetchResult result =
                PricingFetchResult.success(75000);

        when(priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                List.of("gaming", "16gb")
        )).thenReturn(result);

        priceFetchWorker.run(job);

        verify(priceEstimationService).fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                List.of("gaming", "16gb")
        );
        verify(itemPricingService).completePricing(
                1L,
                4L,
                result
        );
        verifyNoMoreInteractions(priceEstimationService, itemPricingService);
    }

    @Test
    void shouldFailPricingWhenPriceProviderThrows() {
        when(priceEstimationService.fetchEstimatedNewPrice(
                anyString(),
                any(),
                nullable(String.class),
                anyList()
        )).thenThrow(new RuntimeException("provider crashed"));

        priceFetchWorker.run(job);

        verify(itemPricingService).failPricing(
                1L,
                4L,
                "WORKER_EXCEPTION",
                "The market pricing job failed unexpectedly."
        );
        verify(itemPricingService, never())
                .completePricing(anyLong(), anyLong(), any());
    }

    @Test
    void shouldPassFailureResultToCompletePricing() {
        PricingFetchResult failure =
                PricingFetchResult.failure(
                        smarttrade.backend.service.pricing.PricingFetchStatus.NETWORK_ERROR,
                        "SERP_API_NETWORK_ERROR",
                        "Provider unavailable.",
                        null
                );

        when(priceEstimationService.fetchEstimatedNewPrice(
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                List.of("gaming", "16gb")
        )).thenReturn(failure);

        priceFetchWorker.run(job);

        verify(itemPricingService).completePricing(
                1L,
                4L,
                failure
        );
        verify(itemPricingService, never())
                .failPricing(anyLong(), anyLong(), anyString(), anyString());
    }
}
