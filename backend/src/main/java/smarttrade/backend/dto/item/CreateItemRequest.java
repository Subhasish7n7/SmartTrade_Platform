package smarttrade.backend.dto.item;

import lombok.Data;

import java.util.List;

@Data
public class CreateItemRequest {

    private String itemName;
    private Integer userPrice;
    private String description;
    private String category;
    private String condition;
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