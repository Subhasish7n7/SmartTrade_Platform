package smarttrade.backend.dto.admin;

import java.util.List;

public record AdminDashboardResponse(
        UserStats users,
        ItemStats items,
        TradeStats trades,
        DisputeStats disputes,
        double averageTrustScore,
        long lowTrustUserCount,
        List<AdminDisputeSummaryResponse> pendingDisputes,
        List<AdminAuditLogResponse> recentAdminActions
) {
    public record UserStats(long total, long active, long suspended, long admins) {}
    public record ItemStats(long total, long visible, long hidden, long available) {}
    public record TradeStats(long open, long negotiating, long accepted,
                             long disputed, long completed, long cancelled,
                             long expired) {}
    public record DisputeStats(long open, long underReview, long resolved) {}
}