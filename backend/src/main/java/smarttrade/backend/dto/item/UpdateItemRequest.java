package smarttrade.backend.dto.item;

import lombok.Data;

import java.util.List;

@Data
public class UpdateItemRequest {

    private String itemName;
    private Integer newPrice;
    private Integer generatedPrice;
    private Integer userPrice;
    private String description;
    private String category;
    private String condition;
    private List<String> labels;
    private List<String> imageUrls;
    private Double latitude;
    private Double longitude;
    private Boolean forTrade;
    private Boolean forSale;
}