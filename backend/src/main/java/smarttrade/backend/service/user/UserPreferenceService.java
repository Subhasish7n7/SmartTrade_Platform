package smarttrade.backend.service.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.user.UpdateUserPreferencesRequest;
import smarttrade.backend.dto.user.UserPreferencesResponse;
import smarttrade.backend.entities.notification.NotificationType;
import smarttrade.backend.entities.user.*;
import smarttrade.backend.repository.user.UserPreferenceRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {
    private final UserPreferenceRepo userPreferenceRepo;
    private final UserRepo userRepo;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public UserPreferenceEntity getOrCreateForUser(UserEntity user) {
        return userPreferenceRepo.findByUser_UserId(user.getUserId()).orElseGet(() ->
                userPreferenceRepo.save(UserPreferenceEntity.builder().user(user).build()));
    }

    @Transactional
    public UserPreferencesResponse getMyPreferences() {
        return toResponse(getOrCreateForUser(authenticatedUserService.getCurrentUser()));
    }

    @Transactional
    public UserPreferencesResponse updateMyPreferences(UpdateUserPreferencesRequest request) {
        if (request == null) throw new IllegalArgumentException("Preferences request cannot be null");
        UserPreferenceEntity p = getOrCreateForUser(authenticatedUserService.getCurrentUser());

        if (request.getProfileVisibility() != null) p.setProfileVisibility(request.getProfileVisibility());
        if (request.getLocationVisibility() != null) p.setLocationVisibility(request.getLocationVisibility());
        if (request.getInventoryVisibility() != null) p.setInventoryVisibility(request.getInventoryVisibility());
        if (request.getShowOnlineStatus() != null) p.setShowOnlineStatus(request.getShowOnlineStatus());
        if (request.getAcceptTradeRequests() != null) p.setAcceptTradeRequests(request.getAcceptTradeRequests());
        if (request.getAcceptBuyOffers() != null) p.setAcceptBuyOffers(request.getAcceptBuyOffers());
        if (request.getMessagePermission() != null) p.setMessagePermission(request.getMessagePermission());
        if (request.getMinimumTraderTrustScore() != null)
            p.setMinimumTraderTrustScore(request.getMinimumTraderTrustScore());
        if (request.getShowUnavailableListings() != null)
            p.setShowUnavailableListings(request.getShowUnavailableListings());
        if (request.getDefaultListingType() != null) p.setDefaultListingType(request.getDefaultListingType());
        if (request.getNotifyTrades() != null) p.setNotifyTrades(request.getNotifyTrades());
        if (request.getNotifyMessages() != null) p.setNotifyMessages(request.getNotifyMessages());
        if (request.getNotifySystem() != null) p.setNotifySystem(request.getNotifySystem());
        if (request.getLoginAlerts() != null) p.setLoginAlerts(request.getLoginAlerts());

        return toResponse(userPreferenceRepo.save(p));
    }

    @Transactional
    public boolean isNotificationEnabled(Long userId, NotificationType type) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserPreferenceEntity p = getOrCreateForUser(user);
        return switch (type) {
            case CHAT_MESSAGE -> p.isNotifyMessages();
            case TRADE_OFFER_CREATED, TRADE_OFFER_RECEIVED, TRADE_ACCEPTED,
                 TRADE_COMPLETION_REQUESTED, TRADE_COMPLETED, TRADE_CANCELLED,
                 TRADE_EXPIRED, BUY_OFFER_RECEIVED -> p.isNotifyTrades();
            case SYSTEM -> p.isNotifySystem();
        };
    }

    @Transactional
    public UserPreferenceEntity getForUser(UserEntity user) {
        return getOrCreateForUser(user);
    }

    private UserPreferencesResponse toResponse(UserPreferenceEntity p) {
        return UserPreferencesResponse.builder()
                .profileVisibility(p.getProfileVisibility())
                .locationVisibility(p.getLocationVisibility())
                .inventoryVisibility(p.getInventoryVisibility())
                .showOnlineStatus(p.isShowOnlineStatus())
                .acceptTradeRequests(p.isAcceptTradeRequests())
                .acceptBuyOffers(p.isAcceptBuyOffers())
                .messagePermission(p.getMessagePermission())
                .minimumTraderTrustScore(p.getMinimumTraderTrustScore())
                .showUnavailableListings(p.isShowUnavailableListings())
                .defaultListingType(p.getDefaultListingType())
                .notifyTrades(p.isNotifyTrades())
                .notifyMessages(p.isNotifyMessages())
                .notifySystem(p.isNotifySystem())
                .loginAlerts(p.isLoginAlerts())
                .build();
    }
}
