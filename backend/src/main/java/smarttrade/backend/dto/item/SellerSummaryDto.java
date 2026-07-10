package smarttrade.backend.dto.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerSummaryDto {
    private Long userId;
    private String name;
    private Double trustScore;
    private Integer totalListings;
    private Integer successfulTrades;
}