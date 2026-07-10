package smarttrade.backend.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private SellerSummaryDto seller;
    private String description;              // Item description
    private String category;                 // Category (e.g., electronics, books)
    private String condition;                // "new", "used - like new", "used - good", etc.
    private List<String> labels;            // Keywords, e.g., ["gaming", "intel", "DDR4"]
    private List<String> imageUrls;
    private Double latitude;                // For location-based filtering
    private Double longitude;
    private String city;
    private String state;
    private String locality;
    private boolean available = true;     // Mark item as available/traded
    private boolean forTrade = false;     // Whether item is marked for trade
    private boolean forSale = true;       // Whether item is marked for sale
    private LocalDateTime createdAt;
}
