package smarttrade.backend.UnitTest.Pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemPricingStatus;
import smarttrade.backend.service.pricing.PriceFetchWorker;
import smarttrade.backend.service.pricing.PricingBackgroundService;
import smarttrade.backend.service.pricing.PricingJobDecision;
import smarttrade.backend.service.pricing.PricingJobRequest;
import smarttrade.backend.service.pricing.PricingJobService;
import smarttrade.backend.service.pricing.PricingRefreshOutcome;
import smarttrade.backend.service.pricing.PricingRefreshResponse;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingBackgroundServiceTest {

    @Mock
    private PricingJobService pricingJobService;

    @Mock
    private PriceFetchWorker priceFetchWorker;

    @InjectMocks
    private PricingBackgroundService pricingBackgroundService;

    private PricingJobRequest job;

    @BeforeEach
    void setUp() {
        job = new PricingJobRequest(
                1L,
                2L,
                "Laptop",
                ItemCategory.COMPUTERS,
                null,
                List.of("gaming"),
                List.of(), // primarySpecifications
                List.of(), // secondarySpecifications
                List.of()  // tertiarySpecifications
        );
    }

    @Test
    void shouldRequestAndExecuteReturnedJob() {
        PricingRefreshResponse response = new PricingRefreshResponse(
                1L,
                PricingRefreshOutcome.STARTED,
                ItemPricingStatus.IN_PROGRESS,
                2L,
                50000,
                80000,
                56000,
                null,
                null,
                null,
                null,
                "started"
        );

        when(pricingJobService.requestRefreshInBackground(1L))
                .thenReturn(new PricingJobDecision(response, job));

        pricingBackgroundService.requestAndExecute(1L);

        verify(pricingJobService).requestRefreshInBackground(1L);
        verify(priceFetchWorker).run(job);
    }

    @Test
    void shouldNotExecuteWhenBackgroundClaimReturnsNoJob() {
        PricingRefreshResponse response = new PricingRefreshResponse(
                1L,
                PricingRefreshOutcome.ALREADY_RUNNING,
                ItemPricingStatus.IN_PROGRESS,
                2L,
                50000,
                80000,
                56000,
                null,
                null,
                null,
                null,
                "already running"
        );

        when(pricingJobService.requestRefreshInBackground(1L))
                .thenReturn(new PricingJobDecision(response, null));

        pricingBackgroundService.requestAndExecute(1L);

        verify(pricingJobService).requestRefreshInBackground(1L);
        verifyNoInteractions(priceFetchWorker);
    }

    @Test
    void shouldExecuteNonNullJobDirectly() {
        pricingBackgroundService.execute(job);

        verify(priceFetchWorker).run(job);
    }

    @Test
    void shouldIgnoreNullJobOnExecute() {
        pricingBackgroundService.execute(null);

        verifyNoInteractions(priceFetchWorker);
    }
}
