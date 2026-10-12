package smarttrade.backend.dto.admin;

import java.util.List;

public record AdminItemDetailsResponse(
        AdminItemSummaryResponse item,
        String description,
        List<String> labels,
        List<AdminAuditLogResponse> recentAdminActions
) {
}