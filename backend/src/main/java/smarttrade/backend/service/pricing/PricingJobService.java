package smarttrade.backend.service.pricing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemPricingStatus;
import smarttrade.backend.entities.item.ItemSpecification;
import smarttrade.backend.entities.item.SpecificationTier;
import smarttrade.backend.repository.item.ItemRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingJobService {

    private final ItemRepo itemRepo;

    @Value("${pricing.refresh-window-hours:168}")
    private long refreshWindowHours;

    @Transactional
    public PricingJobDecision requestRefresh(Long itemId) {
        ItemEntity item = itemRepo.findByIdForUpdate(itemId).orElse(null);

        if (item == null) {
            return PricingJobDecision.notFound(itemId);
        }

        return claimCurrentVersion(item);
    }

    @Transactional
    public PricingJobDecision requestRefreshInBackground(Long itemId) {
        ItemEntity item = itemRepo.findByIdForUpdate(itemId).orElse(null);
        if (item == null) return PricingJobDecision.notFound(itemId);
        return claimCurrentVersion(item);
    }

    private PricingJobDecision claimCurrentVersion(ItemEntity item) {
        long currentVersion = item.getPricingVersion() == null
                ? 0L
                : item.getPricingVersion();

        ItemPricingStatus currentStatus = item.getPricingStatus() == null
                ? ItemPricingStatus.PENDING
                : item.getPricingStatus();

        if (currentStatus == ItemPricingStatus.IN_PROGRESS) {
            return PricingJobDecision.alreadyRunning(
                    item, currentVersion, nextPriceRefreshAt(item)
            );
        }

        if (isPriceFresh(item)) {
            return PricingJobDecision.alreadyFresh(
                    item, currentVersion, nextPriceRefreshAt(item)
            );
        }

        if (currentVersion <= 0L) {
            currentVersion = 1L;
            item.setPricingVersion(currentVersion);
        }

        item.setPricingStatus(ItemPricingStatus.IN_PROGRESS);
        item.setPricingErrorCode(null);
        item.setPricingErrorMessage(null);
        itemRepo.save(item);

        PricingJobRequest job = new PricingJobRequest(
                item.getItemId(),
                currentVersion,
                item.getItemName(),
                item.getCategory(),
                item.getCustomCategory(),
                item.getLabels() == null ? List.of() : List.copyOf(item.getLabels()),
                specificationsFor(item, SpecificationTier.PRIMARY),
                specificationsFor(item, SpecificationTier.SECONDARY),
                specificationsFor(item, SpecificationTier.TERTIARY)
        );

        return PricingJobDecision.started(
                item, currentVersion, nextPriceRefreshAt(item), job
        );
    }

    private List<String> specificationsFor(ItemEntity item, SpecificationTier tier) {
        if (item.getSpecifications() == null || item.getSpecifications().isEmpty()) {
            return List.of();
        }

        return item.getSpecifications().stream()
                .filter(specification -> specification.getTier() == tier)
                .map(ItemSpecification::getSpecification)
                .toList();
    }

    public boolean isPriceFresh(ItemEntity item) {
        if (item.getPricingStatus() != ItemPricingStatus.READY
                || item.getPriceFetchedAt() == null) {
            return false;
        }

        return LocalDateTime.now()
                .isBefore(item.getPriceFetchedAt().plusHours(refreshWindowHours));
    }

    public LocalDateTime nextPriceRefreshAt(ItemEntity item) {
        if (item.getPriceFetchedAt() == null) return null;
        return item.getPriceFetchedAt().plusHours(refreshWindowHours);
    }
}
