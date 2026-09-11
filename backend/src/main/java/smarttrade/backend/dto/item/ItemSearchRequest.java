package smarttrade.backend.dto.item;

import lombok.Data;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ListingType;

import java.util.List;

@Data
public class ItemSearchRequest {
    private String name;
    private ItemCategory category;
    private List<ItemCondition> conditions;
    private List<String> labels;
    private Integer minPrice;
    private Integer maxPrice;
    private ListingType listingType;
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private ItemSort sort = ItemSort.RELEVANCE;
    private int page = 0;
    private int size = 24;
}