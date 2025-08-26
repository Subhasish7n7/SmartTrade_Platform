package smarttrade.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import smarttrade.backend.entities.userEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class itemDto {
    private Long item_id;
    private String item_name;
    private int item_NewPrice;
    private int item_GeneratedPrice;
    private int item_UserPrice;
    private userDto user;
    private String description;              // Item description
    private String category;                 // Category (e.g., electronics, books)
    private String condition;                // "new", "used - like new", "used - good", etc.
    private List<String> labels;            // Keywords, e.g., ["gaming", "intel", "DDR4"]
    private Double latitude;                // For location-based filtering
    private Double longitude;
    private boolean isAvailable = true;     // Mark item as available/traded
    private boolean isForTrade = false;     // Whether item is marked for trade
    private boolean isForSale = true;       // Whether item is marked for sale
}
