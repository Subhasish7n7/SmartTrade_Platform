package smarttrade.backend.dto.admin;

import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemModerationStatus;
import smarttrade.backend.entities.item.ItemPricingStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdminItemSummaryResponse(
        Long itemId,
        String itemName,
        Long sellerId,
        String sellerName,
        String sellerEmail,
        Integer userPrice,
        Integer newPrice,
        Integer generatedPrice,
        ItemPricingStatus pricingStatus,
        ItemCategory category,
        ItemCondition condition,
        boolean forSale,
        boolean forTrade,
        boolean available,
        boolean sold,
        boolean traded,
        ItemModerationStatus moderationStatus,
        String city,
        String state,
        String locality,
        List<String> imageUrls,
        LocalDateTime createdAt
) {}