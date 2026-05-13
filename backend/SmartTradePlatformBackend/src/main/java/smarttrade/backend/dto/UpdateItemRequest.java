package smarttrade.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateItemRequest {

    private String item_name;
    private Integer item_NewPrice;
    private Integer item_GeneratedPrice;
    private Integer item_UserPrice;
    private String description;
    private String category;
    private String condition;
    private List<String> labels;
    private Double latitude;
    private Double longitude;
    private Boolean forTrade;
    private Boolean forSale;
}