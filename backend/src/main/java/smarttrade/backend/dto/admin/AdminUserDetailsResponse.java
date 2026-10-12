package smarttrade.backend.dto.admin;

import java.util.List;

public record AdminUserDetailsResponse(
        AdminUserSummaryResponse user,
        List<AdminAuditLogResponse> recentAdminActions
) {
}