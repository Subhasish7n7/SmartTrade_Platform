package smarttrade.backend.dto.user;

import lombok.*;
import smarttrade.backend.entities.user.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesResponse {
    private ProfileVisibility profileVisibility;
    private LocationVisibility locationVisibility;
    private InventoryVisibility inventoryVisibility;
    private boolean showOnlineStatus;
    private boolean acceptTradeRequests;
    private boolean acceptBuyOffers;
    private MessagePermission messagePermission;
    private int minimumTraderTrustScore;
    private boolean showUnavailableListings;
    private DefaultListingType defaultListingType;
    private boolean notifyTrades;
    private boolean notifyMessages;
    private boolean notifySystem;
    private boolean loginAlerts;
}
