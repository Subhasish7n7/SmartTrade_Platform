package smarttrade.backend.dto.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;

import java.util.List;

@Data
public class CreateItemRequest {
    private String itemName;

    @NotNull
    @Positive
    private Integer userPrice;
    private String description;

    @NotNull
    private ItemCategory category;

    @NotNull
    private ItemCondition condition;

    private String customCategory;
    private List<String> labels;

    @Valid
    @Size(max = 8)
    private List<ItemImageRequest> images;

    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String locality;
    private boolean forTrade = false;
    private boolean forSale = true;
}