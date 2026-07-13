package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.dto.AuditFinancierDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditFinancierService {

    private final AuditFinancierRepository auditFinancierRepository;

    @Transactional
    public AuditFinancierDTO log(String entityType, UUID entityId, String action,
                                  UUID userId, String userName,
                                  String oldValues, String newValues) {
        AuditFinancier audit = AuditFinancier.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .userId(userId)
                .userName(userName)
                .oldValues(oldValues)
                .newValues(newValues)
                .createdAt(LocalDateTime.now())
                .build();

        return toDTO(auditFinancierRepository.save(audit));
    }

    public List<AuditFinancierDTO> getByEntity(String entityType, UUID entityId) {
        return auditFinancierRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream().map(this::toDTO).toList();
    }

    public List<AuditFinancierDTO> getAll(String entityType) {
        if (entityType != null && !entityType.isBlank()) {
            return auditFinancierRepository.findByEntityTypeOrderByCreatedAtDesc(entityType)
                    .stream().map(this::toDTO).toList();
        }
        return auditFinancierRepository.findAll().stream().map(this::toDTO).toList();
    }

    private AuditFinancierDTO toDTO(AuditFinancier audit) {
        return AuditFinancierDTO.builder()
                .id(audit.getId())
                .entityType(audit.getEntityType())
                .entityId(audit.getEntityId())
                .action(audit.getAction())
                .userId(audit.getUserId())
                .userName(audit.getUserName())
                .oldValues(audit.getOldValues())
                .newValues(audit.getNewValues())
                .createdAt(audit.getCreatedAt())
                .build();
    }
}
