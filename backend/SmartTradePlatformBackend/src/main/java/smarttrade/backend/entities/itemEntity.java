package smarttrade.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="items")
public class itemEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long item_id;
    private String item_name;
    private int item_NewPrice;
    private int item_GeneratedPrice;
    private int item_UserPrice;
    @ManyToOne
    @JoinColumn(name = "userID")
    private userEntity user;

    private String description;              // Item description
    private String category;                 // Category (e.g., electronics, books)
    private String condition;                // "new", "used - like new", "used - good", etc.
    private List<String> labels;// Keywords, e.g., ["gaming", "intel", "DDR4"]
    @JsonIgnore
    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;
    private boolean isAvailable = true;     // Mark item as available/traded
    private boolean isForTrade = false;     // Whether item is marked for trade
    private boolean isForSale = true;       // Whether item is marked for sale

}
