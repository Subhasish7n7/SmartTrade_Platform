package smarttrade.backend.dto.admin;

import smarttrade.backend.entities.item.ItemCondition;

public record AdminTradeItemResponse(
        Long itemId,
        String itemName,
        Long ownerId,
        String ownerName,
        Integer userPrice,
        Integer generatedPrice,
        ItemCondition condition
) {}