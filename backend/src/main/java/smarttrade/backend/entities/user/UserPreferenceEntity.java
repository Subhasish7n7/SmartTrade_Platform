
package smarttrade.backend.entities.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_preferences_user",
                columnNames = "user_id"
        )
)
public class UserPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false, length = 20)
    private ProfileVisibility profileVisibility =
            ProfileVisibility.PUBLIC;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "location_visibility", nullable = false, length = 20)
    private LocationVisibility locationVisibility =
            LocationVisibility.CITY;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_visibility", nullable = false, length = 20)
    private InventoryVisibility inventoryVisibility =
            InventoryVisibility.PUBLIC;

    @Builder.Default
    @Column(name = "show_online_status", nullable = false)
    private boolean showOnlineStatus = true;

    @Builder.Default
    @Column(name = "accept_trade_requests", nullable = false)
    private boolean acceptTradeRequests = true;

    @Builder.Default
    @Column(name = "accept_buy_offers", nullable = false)
    private boolean acceptBuyOffers = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "message_permission", nullable = false, length = 20)
    private MessagePermission messagePermission =
            MessagePermission.EVERYONE;

    @Builder.Default
    @Column(name = "minimum_trader_trust_score", nullable = false)
    private int minimumTraderTrustScore = 0;

    @Builder.Default
    @Column(name = "show_unavailable_listings", nullable = false)
    private boolean showUnavailableListings = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "default_listing_type", nullable = false, length = 20)
    private DefaultListingType defaultListingType =
            DefaultListingType.SALE;

    @Builder.Default
    @Column(name = "notify_trades", nullable = false)
    private boolean notifyTrades = true;

    @Builder.Default
    @Column(name = "notify_messages", nullable = false)
    private boolean notifyMessages = true;

    @Builder.Default
    @Column(name = "notify_system", nullable = false)
    private boolean notifySystem = true;

    @Builder.Default
    @Column(name = "login_alerts", nullable = false)
    private boolean loginAlerts = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enforce required preference values before database insertion.
     * Handles explicit nulls as well as fields left uninitialized.
     */
    private void applyDefaults() {
        if (profileVisibility == null) {
            profileVisibility = ProfileVisibility.PUBLIC;
        }

        if (locationVisibility == null) {
            locationVisibility = LocationVisibility.CITY;
        }

        if (inventoryVisibility == null) {
            inventoryVisibility = InventoryVisibility.PUBLIC;
        }

        if (messagePermission == null) {
            messagePermission = MessagePermission.EVERYONE;
        }

        if (defaultListingType == null) {
            defaultListingType = DefaultListingType.SALE;
        }
    }

    @PrePersist
    private void prePersist() {
        applyDefaults();

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void preUpdate() {
        applyDefaults();

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }
}
