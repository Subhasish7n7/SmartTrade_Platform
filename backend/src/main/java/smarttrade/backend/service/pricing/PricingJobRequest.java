package smarttrade.backend.service.pricing;

import smarttrade.backend.entities.item.ItemCategory;

import java.util.List;

/**
 * Immutable pricing-input snapshot captured for one pricing version.
 */
public record PricingJobRequest(
        Long itemId,
        long pricingVersion,
        String itemName,
        ItemCategory category,
        String customCategory,
        List<String> labels,
        List<String> primarySpecifications,
        List<String> secondarySpecifications,
        List<String> tertiarySpecifications
) {
}
