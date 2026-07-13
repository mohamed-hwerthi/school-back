package com.schoolSys.schooolSys.common.audit;

import java.util.UUID;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for audit log API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private UUID id;
    private String username;
    private String action;
    private String entityType;
    private UUID entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;

    /**
     * Maps an {@link AuditLog} entity to its DTO.
     */
    public static AuditLogDTO fromEntity(AuditLog entity) {
        return AuditLogDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .details(entity.getDetails())
                .ipAddress(entity.getIpAddress())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
