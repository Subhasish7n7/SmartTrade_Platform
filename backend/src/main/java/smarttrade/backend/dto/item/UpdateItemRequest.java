package smarttrade.backend.dto.item;

import lombok.Data;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;

import java.util.List;

@Data
public class UpdateItemRequest {

    private String itemName;
    private Integer userPrice;
    private String description;
    private ItemCategory category;
    private ItemCondition condition;
    private String customCategory;
    private List<String> labels;
    private List<String> imageUrls;
    private Double latitude;
    private Double longitude;
    private Boolean forTrade;
    private Boolean forSale;
}