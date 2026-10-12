package smarttrade.backend.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import smarttrade.backend.entities.item.ItemCategory;
import smarttrade.backend.entities.item.ItemCondition;
import smarttrade.backend.entities.item.ItemPricingStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private Long itemId;
    private String itemName;
    private Integer newPrice;
    private Integer generatedPrice;
    private Integer userPrice;
    private ItemPricingStatus pricingStatus;
    private LocalDateTime priceFetchedAt;
    private LocalDateTime nextPriceRefreshAt;
    private String pricingErrorCode;
    private String pricingErrorMessage;
    private SellerSummaryDto seller;
    private String description;
    private ItemCategory category;
    private ItemCondition condition;
    private String customCategory;
    private List<String> primarySpecifications;
    private List<String> secondarySpecifications;
    private List<String> tertiarySpecifications;
    private List<String> labels;
    private List<ItemImageResponse> images;
    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String locality;
    private boolean available = true;
    private boolean forTrade = false;
    private boolean forSale = true;
    private LocalDateTime createdAt;
}