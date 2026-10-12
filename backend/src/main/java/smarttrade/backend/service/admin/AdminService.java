package smarttrade.backend.service.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.admin.*;
import smarttrade.backend.entities.admin.AdminAuditAction;
import smarttrade.backend.entities.admin.AdminAuditLogEntity;
import smarttrade.backend.entities.admin.AdminTargetType;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.item.ItemImageEntity;
import smarttrade.backend.entities.item.ItemImageStatus;
import smarttrade.backend.entities.item.ItemModerationStatus;
import smarttrade.backend.entities.trade.*;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.user.UserRole;
import smarttrade.backend.repository.*;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.repository.trades.TradeDisputeRepo;
import smarttrade.backend.repository.trades.TradeOfferRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepo userRepo;
    private final ItemRepo itemRepo;
    private final TradeRepo tradeRepo;
    private final TradeOfferRepo tradeOfferRepo;
    private final TradeDisputeRepo disputeRepo;
    private final AdminAuditLogRepo auditLogRepo;
    private final AdminAuditService adminAuditService;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public AdminDashboardResponse getDashboard() {

        AdminDashboardResponse.UserStats users =
                new AdminDashboardResponse.UserStats(
                        userRepo.count(),
                        userRepo.countByAccountStatus(AccountStatus.ACTIVE),
                        userRepo.countByAccountStatus(AccountStatus.SUSPENDED),
                        userRepo.countByRole(UserRole.ADMIN)
                );

        AdminDashboardResponse.ItemStats items =
                new AdminDashboardResponse.ItemStats(
                        itemRepo.count(),
                        itemRepo.countByModerationStatus(ItemModerationStatus.ACTIVE),
                        itemRepo.countByModerationStatus(ItemModerationStatus.HIDDEN),
                        itemRepo.countByAvailableTrue()
                );

        AdminDashboardResponse.TradeStats trades =
                new AdminDashboardResponse.TradeStats(
                        tradeRepo.countByStatus(TradeStatus.OPEN),
                        tradeRepo.countByStatus(TradeStatus.NEGOTIATING),
                        tradeRepo.countByStatus(TradeStatus.ACCEPTED),
                        tradeRepo.countByStatus(TradeStatus.DISPUTED),
                        tradeRepo.countByStatus(TradeStatus.COMPLETED),
                        tradeRepo.countByStatus(TradeStatus.CANCELLED),
                        tradeRepo.countByStatus(TradeStatus.EXPIRED)
                );

        AdminDashboardResponse.DisputeStats disputes =
                new AdminDashboardResponse.DisputeStats(
                        disputeRepo.countByStatus(DisputeStatus.OPEN),
                        disputeRepo.countByStatus(DisputeStatus.UNDER_REVIEW),
                        disputeRepo.countByStatus(DisputeStatus.RESOLVED)
                );

        List<AdminDisputeSummaryResponse> pendingDisputes =
                disputeRepo.findTop5ByStatusInOrderByCreatedAtDesc(
                                List.of(
                                        DisputeStatus.OPEN,
                                        DisputeStatus.UNDER_REVIEW
                                )
                        )
                        .stream()
                        .map(this::mapDisputeSummary)
                        .toList();

        List<AdminAuditLogResponse> recentActions =
                auditLogRepo.findTop10ByOrderByCreatedAtDesc()
                        .stream()
                        .map(this::mapAudit)
                        .toList();

        Double averageTrust = userRepo.findAverageTrustScore();

        return new AdminDashboardResponse(
                users,
                items,
                trades,
                disputes,
                averageTrust == null ? 500.0 : averageTrust,
                userRepo.countByTrustScoreLessThan(300.0),
                pendingDisputes,
                recentActions
        );
    }

    @Transactional
    public AdminPageResponse<AdminUserSummaryResponse> getUsers(
            int page,
            int size,
            String search,
            UserRole role,
            AccountStatus status
    ) {
        String normalizedSearch = normalize(search);
        Pageable pageable = pageable(page, size);

        Page<UserEntity> result;

        if (normalizedSearch == null && role == null && status == null) {
            result = userRepo.findAllByOrderByCreatedAtDesc(pageable);

        } else if (normalizedSearch == null) {
            result = userRepo.searchAdminUsersByFilters(
                    role,
                    status,
                    pageable
            );

        } else {
            result = userRepo.searchAdminUsers(
                    normalizedSearch,
                    role,
                    status,
                    pageable
            );
        }

        return AdminPageResponse.from(result.map(this::mapUser));
    }

    @Transactional
    public AdminUserDetailsResponse getUser(Long userId) {

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        List<AdminAuditLogResponse> audit =
                auditLogRepo
                        .findTop20ByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                                AdminTargetType.USER,
                                userId
                        )
                        .stream()
                        .map(this::mapAudit)
                        .toList();

        return new AdminUserDetailsResponse(
                mapUser(user),
                audit
        );
    }

    @Transactional
    public void suspendUser(Long userId, String reason) {

        UserEntity admin = authenticatedUserService.getCurrentUser();

        UserEntity user = userRepo.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (admin.getUserId().equals(user.getUserId())) {
            throw new IllegalStateException(
                    "An admin cannot suspend their own account."
            );
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException(
                    "Admin accounts are not suspended through normal user moderation."
            );
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) return;

        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepo.save(user);

        adminAuditService.record(
                AdminAuditAction.USER_SUSPENDED,
                AdminTargetType.USER,
                userId,
                reason,
                null
        );
    }

    @Transactional
    public void restoreUser(Long userId, String reason) {

        UserEntity user = userRepo.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException(
                    "Admin accounts do not use normal user restoration."
            );
        }

        if (user.getAccountStatus() == AccountStatus.ACTIVE) return;

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepo.save(user);

        adminAuditService.record(
                AdminAuditAction.USER_RESTORED,
                AdminTargetType.USER,
                userId,
                reason,
                null
        );
    }

    @Transactional
    public AdminPageResponse<AdminItemSummaryResponse> getItems(
            int page,
            int size,
            String search,
            ItemModerationStatus moderationStatus,
            Boolean available
    ) {
        String normalizedSearch = normalize(search);
        Pageable pageable = pageable(page, size);

        Page<ItemEntity> result;

        if (normalizedSearch == null
                && moderationStatus == null
                && available == null) {

            result = itemRepo.findAllByOrderByCreatedAtDesc(pageable);

        } else if (normalizedSearch == null) {

            result = itemRepo.searchAdminItemsByFilters(
                    moderationStatus,
                    available,
                    pageable
            );

        } else {

            result = itemRepo.searchAdminItems(
                    normalizedSearch,
                    moderationStatus,
                    available,
                    pageable
            );
        }

        return AdminPageResponse.from(result.map(this::mapItem));
    }

    @Transactional
    public AdminItemDetailsResponse getItem(Long itemId) {

        ItemEntity item = itemRepo.findById(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Item not found")
                );

        List<AdminAuditLogResponse> audit =
                auditLogRepo
                        .findTop20ByTargetTypeAndTargetIdOrderByCreatedAtDesc(
                                AdminTargetType.ITEM,
                                itemId
                        )
                        .stream()
                        .map(this::mapAudit)
                        .toList();

        return new AdminItemDetailsResponse(
                mapItem(item),
                item.getDescription(),
                item.getLabels() == null ? List.of() : List.copyOf(item.getLabels()),
                audit
        );
    }

    @Transactional
    public void hideItem(Long itemId, String reason) {

        ItemEntity item = itemRepo.findByIdForUpdate(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Item not found")
                );

        if (item.getModerationStatus() == ItemModerationStatus.HIDDEN) return;

        item.setModerationStatus(ItemModerationStatus.HIDDEN);
        itemRepo.save(item);

        adminAuditService.record(
                AdminAuditAction.ITEM_HIDDEN,
                AdminTargetType.ITEM,
                itemId,
                reason,
                "ownerUserId=" + item.getUser().getUserId()
        );
    }

    @Transactional
    public void restoreItem(Long itemId, String reason) {

        ItemEntity item = itemRepo.findByIdForUpdate(itemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Item not found")
                );

        if (item.getModerationStatus() == ItemModerationStatus.ACTIVE) return;

        item.setModerationStatus(ItemModerationStatus.ACTIVE);
        itemRepo.save(item);

        adminAuditService.record(
                AdminAuditAction.ITEM_RESTORED,
                AdminTargetType.ITEM,
                itemId,
                reason,
                null
        );
    }

    @Transactional
    public AdminPageResponse<AdminTradeSummaryResponse> getTrades(
            int page,
            int size,
            String search,
            TradeStatus status
    ) {
        String normalizedSearch = normalize(search);
        Pageable pageable = pageable(page, size);

        Page<TradeEntity> result;

        if (normalizedSearch == null && status == null) {

            result = tradeRepo.findAllByOrderByCreatedAtDesc(pageable);

        } else if (normalizedSearch == null) {

            result = tradeRepo.searchAdminTradesByStatus(
                    status,
                    pageable
            );

        } else {

            result = tradeRepo.searchAdminTrades(
                    normalizedSearch,
                    status,
                    pageable
            );
        }

        return AdminPageResponse.from(result.map(this::mapTrade));
    }

    @Transactional
    public AdminTradeDetailsResponse getTrade(Long tradeId) {

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found")
                );

        List<AdminTradeOfferResponse> offers =
                tradeOfferRepo.findByTrade_TradeIdOrderByCreatedAtAsc(tradeId)
                        .stream()
                        .map(this::mapOffer)
                        .toList();

        AdminDisputeSummaryResponse dispute =
                disputeRepo.findFirstByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                        .map(this::mapDisputeSummary)
                        .orElse(null);

        return new AdminTradeDetailsResponse(
                mapTrade(trade),
                offers,
                dispute
        );
    }

    @Transactional
    public AdminPageResponse<AdminDisputeSummaryResponse> getDisputes(
            int page,
            int size,
            DisputeStatus status
    ) {

        Page<TradeDisputeEntity> result =
                status == null
                        ? disputeRepo.findAllByOrderByCreatedAtDesc(pageable(page, size))
                        : disputeRepo.findByStatus(status, pageable(page, size));

        return AdminPageResponse.from(result.map(this::mapDisputeSummary));
    }

    @Transactional
    public AdminPageResponse<AdminAuditLogResponse> getAuditLogs(
            int page,
            int size,
            AdminAuditAction action,
            AdminTargetType targetType
    ) {

        Pageable pageable = pageable(page, size);
        Page<AdminAuditLogEntity> result;

        if (action != null && targetType != null) {
            result = auditLogRepo.findByActionAndTargetTypeOrderByCreatedAtDesc(
                    action, targetType, pageable
            );
        } else if (action != null) {
            result = auditLogRepo.findByActionOrderByCreatedAtDesc(
                    action, pageable
            );
        } else if (targetType != null) {
            result = auditLogRepo.findByTargetTypeOrderByCreatedAtDesc(
                    targetType, pageable
            );
        } else {
            result = auditLogRepo.findAllByOrderByCreatedAtDesc(pageable);
        }

        return AdminPageResponse.from(result.map(this::mapAudit));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE)
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private AdminUserSummaryResponse mapUser(UserEntity user) {
        return new AdminUserSummaryResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone_no(),
                user.getRole(),
                user.getAccountStatus(),
                user.getTrustScore(),
                user.getTotalListings(),
                user.getSuccessfulTrades(),
                user.getLatitude(),
                user.getLongitude(),
                user.getCreatedAt()
        );
    }

    private AdminItemSummaryResponse mapItem(ItemEntity item) {

        List<String> imageUrls = item.getImages() == null
                ? List.of()
                : item.getImages().stream()
                .filter(image -> image.getStatus() == ItemImageStatus.ACTIVE)
                .sorted(java.util.Comparator.comparing(ItemImageEntity::getSortOrder))
                .map(ItemImageEntity::getSecureUrl)
                .toList();

        return new AdminItemSummaryResponse(
                item.getItemId(),
                item.getItemName(),
                item.getUser().getUserId(),
                item.getUser().getName(),
                item.getUser().getEmail(),
                item.getUserPrice(),
                item.getNewPrice(),
                item.getGeneratedPrice(),
                item.getPricingStatus(),
                item.getCategory(),
                item.getCondition(),
                item.isForSale(),
                item.isForTrade(),
                item.isAvailable(),
                item.isSold(),
                item.isTraded(),
                item.getModerationStatus(),
                item.getCity(),
                item.getState(),
                item.getLocality(),
                imageUrls,
                item.getCreatedAt()
        );
    }

    private AdminTradeSummaryResponse mapTrade(TradeEntity trade) {
        return new AdminTradeSummaryResponse(
                trade.getTradeId(),
                trade.getInitiator().getUserId(),
                trade.getInitiator().getName(),
                trade.getInitiator().getEmail(),
                trade.getReceiver().getUserId(),
                trade.getReceiver().getName(),
                trade.getReceiver().getEmail(),
                trade.getStatus(),
                trade.getInitiatorCompletionConfirmed(),
                trade.getReceiverCompletionConfirmed(),
                trade.getCreatedAt(),
                trade.getAcceptedAt(),
                trade.getCompletionRequestedAt(),
                trade.getCompletedAt(),
                trade.getCancelledAt(),
                trade.getDisputedAt()
        );
    }

    private AdminTradeOfferResponse mapOffer(TradeOfferEntity offer) {
        return new AdminTradeOfferResponse(
                offer.getId(),
                offer.getSender().getUserId(),
                offer.getSender().getName(),
                offer.getReceiver().getUserId(),
                offer.getReceiver().getName(),
                offer.getCashAdjustment(),
                offer.getCreatedAt(),
                offer.getSenderItems().stream().map(this::mapTradeItem).toList(),
                offer.getReceiverItems().stream().map(this::mapTradeItem).toList()
        );
    }

    private AdminTradeItemResponse mapTradeItem(ItemEntity item) {
        return new AdminTradeItemResponse(
                item.getItemId(),
                item.getItemName(),
                item.getUser().getUserId(),
                item.getUser().getName(),
                item.getUserPrice(),
                item.getGeneratedPrice(),
                item.getCondition()
        );
    }

    private AdminDisputeSummaryResponse mapDisputeSummary(TradeDisputeEntity dispute) {
        return new AdminDisputeSummaryResponse(
                dispute.getDisputeId(),
                dispute.getTrade().getTradeId(),
                dispute.getOpenedBy().getUserId(),
                dispute.getOpenedBy().getName(),
                dispute.getStatus(),
                dispute.getReason(),
                dispute.getResolution(),
                dispute.getResolvedBy() == null ? null : dispute.getResolvedBy().getUserId(),
                dispute.getResolvedBy() == null ? null : dispute.getResolvedBy().getName(),
                dispute.getFaultUser() == null ? null : dispute.getFaultUser().getUserId(),
                dispute.getCreatedAt(),
                dispute.getUnderReviewAt(),
                dispute.getResolvedAt()
        );
    }

    private AdminAuditLogResponse mapAudit(AdminAuditLogEntity audit) {
        return new AdminAuditLogResponse(
                audit.getId(),
                audit.getAdminUser().getUserId(),
                audit.getAdminUser().getName(),
                audit.getAction(),
                audit.getTargetType(),
                audit.getTargetId(),
                audit.getReason(),
                audit.getMetadata(),
                audit.getCreatedAt()
        );
    }
}