package smarttrade.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long userId;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;
    private String name;
    private String password;
    private  String phone_no;
    private double trustScore = 0.0;      // Based on correct labeling and pricing
    private int totalListings = 0;
    private int successfulTrades = 0;

    private Double latitude;
    private Double longitude;
}
