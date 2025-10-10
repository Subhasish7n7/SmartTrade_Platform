package smarttrade.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class userDto {
    private Long userId;
    private String email;
    private String name;
    private  String phone_no;
    private double trustScore = 0.0;      // Based on correct labeling and pricing
    private int totalListings = 0;
    private int successfulTrades = 0;

    private Double latitude;
    private Double longitude;
}
