package smarttrade.backend.service.trade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.admin.AdminDisputeDetailsResponse;
import smarttrade.backend.dto.admin.AdminDisputeSummaryResponse;
import smarttrade.backend.dto.admin.AdminTradeSummaryResponse;
import smarttrade.backend.dto.admin.ResolveDisputeRequest;
import smarttrade.backend.dto.trade.CreateDisputeRequest;
import smarttrade.backend.dto.trade.TradeDisputeResponse;
import smarttrade.backend.entities.admin.AdminAuditAction;
import smarttrade.backend.entities.admin.AdminTargetType;
import smarttrade.backend.entities.trade.*;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.events.trust.TrustEventSourceType;
import smarttrade.backend.events.trust.TrustEventType;
import smarttrade.backend.repository.trades.TradeDisputeRepo;
import smarttrade.backend.repository.trades.TradeRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.admin.AdminAuditService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeDisputeService {

    private final TradeRepo tradeRepo;
    private final TradeDisputeRepo disputeRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final TradeFinalizationService tradeFinalizationService;
    private final AdminAuditService adminAuditService;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Transactional
    public TradeDisputeResponse openDispute(
            Long tradeId,
            CreateDisputeRequest request
    ) {

        UserEntity currentUser =
                authenticatedUserService.getCurrentUser();

        TradeEntity trade =
                tradeRepo.findByIdForUpdate(tradeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Trade not found")
                        );

        validateParticipant(trade, currentUser);

        if (trade.getStatus() != TradeStatus.ACCEPTED) {
            throw new IllegalStateException(
                    "A dispute can only be opened for an accepted trade."
            );
        }

        if (disputeRepo.existsByTrade_TradeIdAndStatusIn(
                tradeId,
                List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW)
        )) {
            throw new IllegalStateException(
                    "This trade already has an active dispute."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        TradeDisputeEntity dispute =
                TradeDisputeEntity.builder()
                        .trade(trade)
                        .openedBy(currentUser)
                        .status(DisputeStatus.OPEN)
                        .reason(request.reason())
                        .description(request.description().trim())
                        .createdAt(now)
                        .build();

        dispute = disputeRepo.save(dispute);

        trade.setStatus(TradeStatus.DISPUTED);
        trade.setDisputedAt(now);
        tradeRepo.save(trade);

        return mapUserResponse(dispute);
    }

    @Transactional
    public void autoOpenCompletionDispute(Long tradeId) {

        TradeEntity trade =
                tradeRepo.findByIdForUpdate(tradeId)
                        .orElseThrow();

        if (trade.getStatus() != TradeStatus.ACCEPTED) return;

        if (trade.getInitiatorCompletionConfirmed()
                && trade.getReceiverCompletionConfirmed()) return;

        if (trade.getCompletionRequestedAt() == null) return;

        if (disputeRepo.existsByTrade_TradeIdAndStatusIn(
                tradeId,
                List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW)
        )) return;

        UserEntity openedBy =
                trade.getInitiatorCompletionConfirmed()
                        ? trade.getInitiator()
                        : trade.getReceiver();

        LocalDateTime now = LocalDateTime.now();

        TradeDisputeEntity dispute =
                TradeDisputeEntity.builder()
                        .trade(trade)
                        .openedBy(openedBy)
                        .status(DisputeStatus.OPEN)
                        .reason(DisputeReason.COMPLETION_DISAGREEMENT)
                        .description(
                                "Completion was requested by one participant " +
                                        "but was not confirmed by both participants " +
                                        "within the configured confirmation window."
                        )
                        .createdAt(now)
                        .build();

        disputeRepo.save(dispute);

        trade.setStatus(TradeStatus.DISPUTED);
        trade.setDisputedAt(now);
        tradeRepo.save(trade);
    }

    @Transactional
    public TradeDisputeResponse getUserDispute(Long tradeId) {

        UserEntity currentUser =
                authenticatedUserService.getCurrentUser();

        TradeEntity trade =
                tradeRepo.findById(tradeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Trade not found")
                        );

        validateParticipant(trade, currentUser);

        TradeDisputeEntity dispute =
                disputeRepo.findFirstByTrade_TradeIdOrderByCreatedAtDesc(tradeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No dispute exists for this trade"
                                )
                        );

        return mapUserResponse(dispute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AdminDisputeDetailsResponse getAdminDispute(Long disputeId) {

        TradeDisputeEntity dispute =
                disputeRepo.findById(disputeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Dispute not found")
                        );

        return mapAdminDetails(dispute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void startReview(Long disputeId, String reason) {

        TradeDisputeEntity dispute =
                disputeRepo.findById(disputeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Dispute not found")
                        );

        if (dispute.getStatus() == DisputeStatus.RESOLVED) {
            throw new IllegalStateException(
                    "Resolved disputes cannot be moved back to review."
            );
        }

        if (dispute.getStatus() == DisputeStatus.UNDER_REVIEW) return;

        dispute.setStatus(DisputeStatus.UNDER_REVIEW);
        dispute.setUnderReviewAt(LocalDateTime.now());
        disputeRepo.save(dispute);

        adminAuditService.record(
                AdminAuditAction.DISPUTE_REVIEW_STARTED,
                AdminTargetType.DISPUTE,
                disputeId,
                reason,
                "tradeId=" + dispute.getTrade().getTradeId()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public AdminDisputeDetailsResponse resolveDispute(
            Long disputeId,
            ResolveDisputeRequest request
    ) {

        TradeDisputeEntity dispute =
                disputeRepo.findById(disputeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Dispute not found")
                        );

        if (dispute.getStatus() == DisputeStatus.RESOLVED) {
            throw new IllegalStateException(
                    "Dispute is already resolved."
            );
        }

        TradeEntity trade =
                tradeRepo.findByIdForUpdate(
                                dispute.getTrade().getTradeId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Trade no longer exists"
                                )
                        );

        UserEntity admin =
                authenticatedUserService.getCurrentUser();

        UserEntity faultUser = null;

        /*
         * faultUserId is meaningful only for CANCEL_WITH_FAULT.
         * Keeping this strict prevents contradictory dispute records.
         */
        if (request.faultUserId() != null
                && request.resolution()
                != DisputeResolution.CANCEL_WITH_FAULT) {

            throw new IllegalArgumentException(
                    "faultUserId is only allowed for CANCEL_WITH_FAULT."
            );
        }

        if (request.resolution()
                == DisputeResolution.CANCEL_WITH_FAULT) {

            if (request.faultUserId() == null) {
                throw new IllegalArgumentException(
                        "faultUserId is required for CANCEL_WITH_FAULT."
                );
            }

            if (trade.getInitiator().getUserId()
                    .equals(request.faultUserId())) {

                faultUser = trade.getInitiator();

            } else if (trade.getReceiver().getUserId()
                    .equals(request.faultUserId())) {

                faultUser = trade.getReceiver();

            } else {

                throw new IllegalArgumentException(
                        "faultUserId must belong to the trade."
                );
            }
        }

        switch (request.resolution()) {

            case MARK_COMPLETED ->

                /*
                 * This remains the single completion path.
                 *
                 * TradeFinalizationService publishes
                 * TRADE_COMPLETED for both participants.
                 */
                    tradeFinalizationService.completeTrade(
                            trade.getTradeId()
                    );

            case CANCEL_NO_FAULT, CANCEL_WITH_FAULT -> {

                trade.setStatus(TradeStatus.CANCELLED);
                trade.setCancelledAt(LocalDateTime.now());
                trade.setCancelledBy(admin);

                tradeRepo.save(trade);
            }

            case REOPEN_TRADE -> {

                trade.setStatus(TradeStatus.ACCEPTED);
                trade.setInitiatorCompletionConfirmed(false);
                trade.setReceiverCompletionConfirmed(false);
                trade.setCompletionRequestedAt(null);

                tradeRepo.save(trade);
            }
        }

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolution(request.resolution());
        dispute.setFaultUser(faultUser);
        dispute.setResolvedBy(admin);
        dispute.setResolutionNotes(
                request.notes() == null
                        ? null
                        : request.notes().trim()
        );
        dispute.setResolvedAt(LocalDateTime.now());

        disputeRepo.save(dispute);

        /*
         * A fault is an authoritative trust signal.
         *
         * CANCEL_NO_FAULT, REOPEN_TRADE, and MARK_COMPLETED do not emit a
         * fault event.
         */
        if (request.resolution()
                == DisputeResolution.CANCEL_WITH_FAULT) {

            applicationEventPublisher.publishEvent(
                    TrustEvent.now(
                            faultUser.getUserId(),
                            TrustEventType.TRADE_FAULT_ASSIGNED,
                            TrustEventSourceType.DISPUTE,
                            disputeId,
                            faultUser.getUserId(),
                            "Trade dispute " + disputeId
                                    + " resolved with fault"
                    )
            );
        }

        adminAuditService.record(
                AdminAuditAction.DISPUTE_RESOLVED,
                AdminTargetType.DISPUTE,
                disputeId,
                request.notes() == null
                        ? request.resolution().name()
                        : request.notes(),
                "tradeId=" + trade.getTradeId()
                        + ",resolution=" + request.resolution()
                        + ",faultUserId="
                        + (faultUser == null
                        ? "null"
                        : faultUser.getUserId())
        );

        return mapAdminDetails(dispute);
    }

    private void validateParticipant(
            TradeEntity trade,
            UserEntity user
    ) {
        boolean participant =
                trade.getInitiator().getUserId().equals(user.getUserId())
                        || trade.getReceiver().getUserId().equals(user.getUserId());

        if (!participant) {
            throw new IllegalStateException("User is not part of this trade.");
        }
    }

    private TradeDisputeResponse mapUserResponse(
            TradeDisputeEntity dispute
    ) {
        return new TradeDisputeResponse(
                dispute.getDisputeId(),
                dispute.getTrade().getTradeId(),
                dispute.getStatus(),
                dispute.getReason(),
                dispute.getDescription(),
                dispute.getResolution(),
                dispute.getResolutionNotes(),
                dispute.getCreatedAt(),
                dispute.getUnderReviewAt(),
                dispute.getResolvedAt()
        );
    }

    private AdminDisputeDetailsResponse mapAdminDetails(
            TradeDisputeEntity dispute
    ) {
        TradeEntity trade = dispute.getTrade();

        AdminDisputeSummaryResponse summary =
                new AdminDisputeSummaryResponse(
                        dispute.getDisputeId(),
                        trade.getTradeId(),
                        dispute.getOpenedBy().getUserId(),
                        dispute.getOpenedBy().getName(),
                        dispute.getStatus(),
                        dispute.getReason(),
                        dispute.getResolution(),
                        dispute.getResolvedBy() == null
                                ? null : dispute.getResolvedBy().getUserId(),
                        dispute.getResolvedBy() == null
                                ? null : dispute.getResolvedBy().getName(),
                        dispute.getFaultUser() == null
                                ? null : dispute.getFaultUser().getUserId(),
                        dispute.getCreatedAt(),
                        dispute.getUnderReviewAt(),
                        dispute.getResolvedAt()
                );

        AdminTradeSummaryResponse tradeSummary =
                new AdminTradeSummaryResponse(
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

        return new AdminDisputeDetailsResponse(
                summary,
                dispute.getDescription(),
                dispute.getResolutionNotes(),
                tradeSummary
        );
    }
}