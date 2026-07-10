package smarttrade.backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;


import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="items")
public class ItemEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name= "item_id")
    private Long itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_new_price")
    private Integer newPrice;

    @Column(name = "item_generated_price")
    private Integer generatedPrice;

    @Column(name = "item_user_price")
    private Integer userPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String description;              // Item description
    private String category;                 // Category (e.g., electronics, books)
    private String condition;                // "new", "used - like new", "used - good", etc.
    @ElementCollection
    @CollectionTable(name = "item_labels", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "label")
    private List<String> labels;             // Keywords, e.g., ["gaming", "intel", "DDR4"]

    @ElementCollection
    @CollectionTable(
            name = "item_images",
            joinColumns = @JoinColumn(name = "item_id")
    )
    @Column(name = "image_url")
    private List<String> imageUrls;

    @JsonIgnore
    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "locality")
    private String locality;

    @Column(name = "is_for_sale")
    private boolean forSale = true;

    @Column(name = "is_for_trade")
    private boolean forTrade = false;

    @Column(name = "is_available")
    private boolean available = true;    // Mark item as available/traded

    @Column(name = "is_sold")
    private boolean sold = false;

    @Column(name = "is_traded")
    private boolean traded = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_locked")
    private boolean locked = false;

    @Column(name = "locked_by_trade")
    private Long lockedByTradeId;

    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
