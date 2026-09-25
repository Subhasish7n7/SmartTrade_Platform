package smarttrade.backend.service.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingBackgroundService {

    private final PricingJobService pricingJobService;
    private final PriceFetchWorker priceFetchWorker;

    @Async("pricingTaskExecutor")
    public void requestAndExecute(Long itemId) {
        PricingJobDecision decision =
                pricingJobService.requestRefreshInBackground(itemId);

        if (decision.job() == null) return;
        priceFetchWorker.run(decision.job());
    }

    @Async("pricingTaskExecutor")
    public void execute(PricingJobRequest job) {
        if (job != null) priceFetchWorker.run(job);
    }
}