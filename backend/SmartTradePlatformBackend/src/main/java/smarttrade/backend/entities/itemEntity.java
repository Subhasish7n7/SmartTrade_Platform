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
    @Column(name= "item_id")
    private Long itemId;

    private String item_name;
    private int item_NewPrice;
    private int item_GeneratedPrice;
    private int item_UserPrice;

    @ManyToOne
    @JoinColumn(name = "userID")
    private UserEntity user;

    private String description;              // Item description
    private String category;                 // Category (e.g., electronics, books)
    private String condition;                // "new", "used - like new", "used - good", etc.
    @ElementCollection
    @CollectionTable(name = "item_labels", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "label")
    private List<String> labels;             // Keywords, e.g., ["gaming", "intel", "DDR4"]
    @JsonIgnore
    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column(name = "is_for_sale")
    private boolean forSale = true;

    @Column(name = "is_for_trade")
    private boolean forTrade = false;

    @Column(name = "is_available")
    private boolean available = true;    // Mark item as available/traded

    @Column(name = "is_locked")
    private boolean locked = false;

    @Column(name = "locked_by_trade")
    private Long lockedByTradeId;

}
