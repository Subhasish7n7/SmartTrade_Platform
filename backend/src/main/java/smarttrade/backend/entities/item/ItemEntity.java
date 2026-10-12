package smarttrade.backend.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.locationtech.jts.geom.Point;
import smarttrade.backend.entities.user.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "items")
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @NotBlank
    @Column(nullable = false)
    private String itemName;

    @Column(name = "item_new_price")
    private Integer newPrice;

    @Column(name = "item_generated_price")
    private Integer generatedPrice;

    @NotNull
    @Positive
    @Column(name = "item_user_price", nullable = false)
    private Integer userPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_status", nullable = false)
    @Builder.Default
    private ItemPricingStatus pricingStatus = ItemPricingStatus.PENDING;

    @Column(name = "price_fetched_at")
    private LocalDateTime priceFetchedAt;

    @Column(name = "pricing_error_code", length = 100)
    private String pricingErrorCode;

    @Column(name = "pricing_error_message", columnDefinition = "TEXT")
    private String pricingErrorMessage;

    @Column(name = "pricing_version", nullable = false)
    @Builder.Default
    private Long pricingVersion = 0L;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCondition condition;

    @ElementCollection
    @CollectionTable(
            name = "item_labels",
            joinColumns = @JoinColumn(name = "item_id")
    )
    @Column(name = "label")
    private List<String> labels;

    /**
     * Seller-declared pricing specifications stored in one collection table.
     * Each row carries its influence tier so the database does not need one table
     * per tier while the API can still expose separate primary/secondary/tertiary lists.
     */
    @ElementCollection
    @CollectionTable(
            name = "item_specifications",
            joinColumns = @JoinColumn(name = "item_id")
    )
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ItemSpecification> specifications = new ArrayList<>();

    @Column(name = "custom_category")
    private String customCategory;

    @OneToMany(
            mappedBy = "item",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = false
    )
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ItemImageEntity> images = new ArrayList<>();

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
    @Builder.Default
    private boolean forSale = true;

    @Column(name = "is_for_trade")
    @Builder.Default
    private boolean forTrade = false;

    @Column(name = "is_available")
    @Builder.Default
    private boolean available = true;

    @Column(name = "is_sold")
    @Builder.Default
    private boolean sold = false;

    @Column(name = "is_traded")
    @Builder.Default
    private boolean traded = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "is_locked")
    @Builder.Default
    private boolean locked = false;

    @Column(name = "locked_by_trade")
    private Long lockedByTradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 20)
    @Builder.Default
    private ItemModerationStatus moderationStatus = ItemModerationStatus.ACTIVE;

    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
