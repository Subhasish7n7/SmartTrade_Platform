package smarttrade.backend.service.pricing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemPricingStatus;
import smarttrade.backend.events.itemPriceFetch.ItemPriceUpdatedEvent;
import smarttrade.backend.repository.item.ItemRepo;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemPricingService {

    private final ItemRepo itemRepo;
    private final PriceEstimationService priceEstimationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void completePricing(Long itemId, long pricingVersion, PricingFetchResult result) {
        ItemEntity item = itemRepo.findByIdForUpdate(itemId).orElse(null);
        if (item == null) return;

        if (item.getPricingVersion() == null || item.getPricingVersion() != pricingVersion) {
            log.info(
                    "Discarding stale pricing result for item {}: jobVersion={}, currentVersion={}",
                    itemId, pricingVersion, item.getPricingVersion()
            );
            return;
        }

        if (item.getPricingStatus() != ItemPricingStatus.IN_PROGRESS) {
            return;
        }

        if (result == null) {
            failPricing(
                    itemId, pricingVersion,
                    "NULL_PRICING_RESULT",
                    "The pricing provider returned no usable result."
            );
            return;
        }

        if (result.status() != PricingFetchStatus.SUCCESS || result.price() == null) {
            item.setPricingStatus(ItemPricingStatus.FAILED);
            item.setPricingErrorCode(
                    result.errorCode() == null ? result.status().name() : result.errorCode()
            );
            item.setPricingErrorMessage(
                    result.errorMessage() == null
                            ? "Market pricing could not be updated."
                            : result.errorMessage()
            );
            itemRepo.save(item);
            return;
        }

        Integer newPrice = result.price();
        Integer generatedPrice = priceEstimationService.generateUsedPrice(
                newPrice, item.getCondition()
        );
        LocalDateTime fetchedAt = LocalDateTime.now();

        item.setNewPrice(newPrice);
        item.setGeneratedPrice(generatedPrice);
        item.setPriceFetchedAt(fetchedAt);
        item.setPricingStatus(ItemPricingStatus.READY);
        item.setPricingErrorCode(null);
        item.setPricingErrorMessage(null);

        itemRepo.save(item);

        applicationEventPublisher.publishEvent(
                new ItemPriceUpdatedEvent(
                        itemId, newPrice, generatedPrice, fetchedAt
                )
        );
    }

    @Transactional
    public void failPricing(
            Long itemId,
            long pricingVersion,
            String errorCode,
            String errorMessage
    ) {
        ItemEntity item = itemRepo.findByIdForUpdate(itemId).orElse(null);
        if (item == null) return;

        if (item.getPricingVersion() == null || item.getPricingVersion() != pricingVersion) return;
        if (item.getPricingStatus() != ItemPricingStatus.IN_PROGRESS) return;

        item.setPricingStatus(ItemPricingStatus.FAILED);
        item.setPricingErrorCode(errorCode);
        item.setPricingErrorMessage(errorMessage);
        itemRepo.save(item);
    }
}