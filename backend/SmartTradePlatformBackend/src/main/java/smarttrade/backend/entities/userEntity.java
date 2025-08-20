package smarttrade.backend.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="users")
public class userEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long user_id;
    private String email;
    private String name;
    private  String phone_no;

    private double trustScore = 0.0;      // Based on correct labeling and pricing
    private int totalListings = 0;
    private int successfulTrades = 0;

    private Double latitude;
    private Double longitude;


}
