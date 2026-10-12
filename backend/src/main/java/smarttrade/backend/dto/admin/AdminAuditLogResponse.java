package smarttrade.backend.dto.admin;

import smarttrade.backend.entities.admin.AdminAuditAction;
import smarttrade.backend.entities.admin.AdminTargetType;

import java.time.LocalDateTime;

public record AdminAuditLogResponse(
        Long id,
        Long adminUserId,
        String adminName,
        AdminAuditAction action,
        AdminTargetType targetType,
        Long targetId,
        String reason,
        String metadata,
        LocalDateTime createdAt
) {}