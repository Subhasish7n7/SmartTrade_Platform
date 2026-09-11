package smarttrade.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyUserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNo;
    private String avatarUrl;
    private double trustScore;
    private int totalListings;
    private int successfulTrades;
}
