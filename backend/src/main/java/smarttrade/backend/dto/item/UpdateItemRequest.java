package smarttrade.backend.dto.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
    @Valid
    @Size(max = 8)
    private List<ItemImageRequest> images;
    private List<Long> removedImageIds;

    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String locality;
    private Boolean clearCoordinates;
    private Boolean forTrade;
    private Boolean forSale;
}