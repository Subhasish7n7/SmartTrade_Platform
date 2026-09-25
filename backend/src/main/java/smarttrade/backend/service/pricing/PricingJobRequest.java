package smarttrade.backend.service.pricing;

import smarttrade.backend.entities.item.ItemCategory;

import java.util.List;

public record PricingJobRequest(
        Long itemId,
        long pricingVersion,
        String itemName,
        ItemCategory category,
        String customCategory,
        List<String> labels
) {
}