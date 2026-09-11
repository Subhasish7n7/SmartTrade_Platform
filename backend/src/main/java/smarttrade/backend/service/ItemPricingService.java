package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.repository.ItemRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemPricingService {

    private final ItemRepo itemRepo;
    private final PriceEstimationService priceEstimationService;

    @Transactional
    public void updatePrices(Long itemId, Integer newPrice) {

        itemRepo.findById(itemId).ifPresent(item -> {

            Integer generatedPrice = priceEstimationService.generateUsedPrice(newPrice, item.getCondition());

            item.setNewPrice(newPrice);
            item.setGeneratedPrice(generatedPrice);

            itemRepo.save(item);

            log.info(
                    "Updated market price for item {}: newPrice={}, generatedPrice={}",
                    itemId,
                    newPrice,
                    generatedPrice
            );
        });
    }
}