package smarttrade.backend.events;

import smarttrade.backend.entities.item.ItemCategory;

import java.util.List;

public record ItemPersistedEvent(
        Long itemId,
        String itemName,
        ItemCategory category,
        String customCategory,
        List<String> labels
) {
}