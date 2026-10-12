package smarttrade.backend.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import smarttrade.backend.entities.user.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserPreferencesRequest {
    private ProfileVisibility profileVisibility;
    private LocationVisibility locationVisibility;
    private InventoryVisibility inventoryVisibility;
    private Boolean showOnlineStatus;
    private Boolean acceptTradeRequests;
    private Boolean acceptBuyOffers;
    private MessagePermission messagePermission;
    @Min(0) @Max(1000) private Integer minimumTraderTrustScore;
    private Boolean showUnavailableListings;
    private DefaultListingType defaultListingType;
    private Boolean notifyTrades;
    private Boolean notifyMessages;
    private Boolean notifySystem;
    private Boolean loginAlerts;
}
