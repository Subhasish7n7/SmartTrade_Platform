package smarttrade.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateItemRequest {

    private String item_name;
    private int item_NewPrice;
    private int item_GeneratedPrice;
    private int item_UserPrice;
    private String description;
    private String category;
    private String condition;
    private List<String> labels;
    private Double latitude;
    private Double longitude;
    private boolean forTrade = false;
    private boolean forSale = true;
}