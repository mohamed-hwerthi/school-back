package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditFinancierDTO {

    private UUID id;
    private String entityType;
    private UUID entityId;
    private String action;
    private UUID userId;
    private String userName;
    private String oldValues;
    private String newValues;
    private LocalDateTime createdAt;
}
