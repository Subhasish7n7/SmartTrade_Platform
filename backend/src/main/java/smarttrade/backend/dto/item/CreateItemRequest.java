package smarttrade.backend.dto.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    /**
     * The first tier should contain only specifications with substantial
     * product/price impact (e.g. RTX 4060, Ryzen 9, 16GB RAM).
     */
    @Size(max = 5)
    private List<String> primarySpecifications;

    /**
     * Secondary specifications help refine comparability without dominating it.
     */
    @Size(max = 8)
    private List<String> secondarySpecifications;

    /**
     * Tertiary specifications are useful context but have the least weight.
     */
    @Size(max = 10)
    private List<String> tertiarySpecifications;

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
