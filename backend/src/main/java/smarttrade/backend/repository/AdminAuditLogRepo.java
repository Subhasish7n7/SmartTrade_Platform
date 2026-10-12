package smarttrade.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.admin.AdminAuditAction;
import smarttrade.backend.entities.admin.AdminAuditLogEntity;
import smarttrade.backend.entities.admin.AdminTargetType;

import java.util.List;

@Repository
public interface AdminAuditLogRepo extends JpaRepository<AdminAuditLogEntity, Long> {

    List<AdminAuditLogEntity> findTop10ByOrderByCreatedAtDesc();

    List<AdminAuditLogEntity> findTop20ByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            AdminTargetType targetType,
            Long targetId
    );

    Page<AdminAuditLogEntity> findByActionAndTargetTypeOrderByCreatedAtDesc(
            AdminAuditAction action,
            AdminTargetType targetType,
            Pageable pageable
    );

    Page<AdminAuditLogEntity> findByActionOrderByCreatedAtDesc(
            AdminAuditAction action,
            Pageable pageable
    );

    Page<AdminAuditLogEntity> findByTargetTypeOrderByCreatedAtDesc(
            AdminTargetType targetType,
            Pageable pageable
    );

    Page<AdminAuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}