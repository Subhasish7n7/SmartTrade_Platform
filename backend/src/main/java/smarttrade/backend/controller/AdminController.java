package smarttrade.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.dto.admin.*;
import smarttrade.backend.entities.admin.AdminAuditAction;
import smarttrade.backend.entities.admin.AdminTargetType;
import smarttrade.backend.entities.item.ItemModerationStatus;
import smarttrade.backend.entities.trade.DisputeStatus;
import smarttrade.backend.entities.trade.TradeStatus;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserRole;
import smarttrade.backend.service.admin.AdminService;
import smarttrade.backend.service.trade.TradeDisputeService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TradeDisputeService tradeDisputeService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<AdminPageResponse<AdminUserSummaryResponse>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) AccountStatus status
    ) {
        return ResponseEntity.ok(
                adminService.getUsers(page, size, search, role, status)
        );
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDetailsResponse> user(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminService.getUser(userId));
    }

    @PatchMapping("/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminService.suspendUser(userId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/restore")
    public ResponseEntity<Void> restoreUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminService.restoreUser(userId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items")
    public ResponseEntity<AdminPageResponse<AdminItemSummaryResponse>> items(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ItemModerationStatus moderationStatus,
            @RequestParam(required = false) Boolean available
    ) {
        return ResponseEntity.ok(
                adminService.getItems(
                        page, size, search, moderationStatus, available
                )
        );
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<AdminItemDetailsResponse> item(
            @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(adminService.getItem(itemId));
    }

    @PatchMapping("/items/{itemId}/hide")
    public ResponseEntity<Void> hideItem(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminService.hideItem(itemId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{itemId}/restore")
    public ResponseEntity<Void> restoreItem(
            @PathVariable Long itemId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        adminService.restoreItem(itemId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trades")
    public ResponseEntity<AdminPageResponse<AdminTradeSummaryResponse>> trades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TradeStatus status
    ) {
        return ResponseEntity.ok(
                adminService.getTrades(page, size, search, status)
        );
    }

    @GetMapping("/trades/{tradeId}")
    public ResponseEntity<AdminTradeDetailsResponse> trade(
            @PathVariable Long tradeId
    ) {
        return ResponseEntity.ok(adminService.getTrade(tradeId));
    }

    @GetMapping("/disputes")
    public ResponseEntity<AdminPageResponse<AdminDisputeSummaryResponse>> disputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) DisputeStatus status
    ) {
        return ResponseEntity.ok(
                adminService.getDisputes(page, size, status)
        );
    }

    @GetMapping("/disputes/{disputeId}")
    public ResponseEntity<AdminDisputeDetailsResponse> dispute(
            @PathVariable Long disputeId
    ) {
        return ResponseEntity.ok(
                tradeDisputeService.getAdminDispute(disputeId)
        );
    }

    @PatchMapping("/disputes/{disputeId}/review")
    public ResponseEntity<Void> reviewDispute(
            @PathVariable Long disputeId,
            @Valid @RequestBody AdminActionRequest request
    ) {
        tradeDisputeService.startReview(disputeId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<AdminDisputeDetailsResponse> resolveDispute(
            @PathVariable Long disputeId,
            @Valid @RequestBody ResolveDisputeRequest request
    ) {
        return ResponseEntity.ok(
                tradeDisputeService.resolveDispute(disputeId, request)
        );
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<AdminPageResponse<AdminAuditLogResponse>> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) AdminAuditAction action,
            @RequestParam(required = false) AdminTargetType targetType
    ) {
        return ResponseEntity.ok(
                adminService.getAuditLogs(page, size, action, targetType)
        );
    }
}