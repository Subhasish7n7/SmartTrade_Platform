package smarttrade.backend.service.admin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.admin.AdminAuditAction;
import smarttrade.backend.entities.admin.AdminAuditLogEntity;
import smarttrade.backend.entities.admin.AdminTargetType;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.AdminAuditLogRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogRepo auditLogRepo;
    private final AuthenticatedUserService authenticatedUserService;

    @Transactional
    public void record(
            AdminAuditAction action,
            AdminTargetType targetType,
            Long targetId,
            String reason,
            String metadata
    ) {

        UserEntity admin =
                authenticatedUserService.getCurrentUser();

        AdminAuditLogEntity audit =
                AdminAuditLogEntity.builder()
                        .adminUser(admin)
                        .action(action)
                        .targetType(targetType)
                        .targetId(targetId)
                        .reason(reason)
                        .metadata(metadata)
                        .createdAt(LocalDateTime.now())
                        .build();

        auditLogRepo.save(audit);
    }
}