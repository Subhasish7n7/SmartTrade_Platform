package smarttrade.backend.dto.item;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;

import java.util.List;

@Data
public class CreateItemRequest {

    private String itemName;
    private Integer userPrice;
    private String description;
    @NotNull
    private ItemCategory category;
    @NotNull
    private ItemCondition condition;
    private String customCategory;
    private List<String> labels;
    private List<String> imageUrls;
    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String locality;
    private boolean forTrade = false;
    private boolean forSale = true;
}