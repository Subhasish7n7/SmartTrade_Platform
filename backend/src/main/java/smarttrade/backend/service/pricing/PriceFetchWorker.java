package smarttrade.backend.service.pricing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceFetchWorker {

    private final PriceEstimationService priceEstimationService;
    private final ItemPricingService itemPricingService;

    public void run(PricingJobRequest job) {
        log.info(
                "Starting pricing fetch for item {} version={}",
                job.itemId(),
                job.pricingVersion()
        );

        try {
            PricingFetchResult result =
                    priceEstimationService.fetchEstimatedNewPrice(
                            job.itemName(),
                            job.category(),
                            job.customCategory(),
                            job.labels()
                    );

            itemPricingService.completePricing(
                    job.itemId(),
                    job.pricingVersion(),
                    result
            );

        } catch (Exception e) {
            log.error(
                    "Pricing worker crashed for item {} version={}",
                    job.itemId(),
                    job.pricingVersion(),
                    e
            );

            itemPricingService.failPricing(
                    job.itemId(),
                    job.pricingVersion(),
                    "WORKER_EXCEPTION",
                    "The market pricing job failed unexpectedly."
            );
        }
    }
}