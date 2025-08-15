package smarttrade.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import smarttrade.backend.entities.userEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class itemDto {
    private Long item_id;
    private String item_name;
    private int item_NewPrice;
    private int item_GeneratedPrice;
    private int item_UserPrice;
    private userEntity user;
}
