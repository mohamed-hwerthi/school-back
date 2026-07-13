package com.schoolSys.schooolSys.tenant.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO returned when reading tenant information.
 */
@Data
@Builder
public class TenantResponseDTO {

    private UUID id;
    private String name;
    private String schemaName;
    private String slug;
    private String contactEmail;
    private boolean active;
    private LocalDateTime createdAt;
    private String plan;
    private Integer maxStudents;
    private Integer maxTeachers;
    private Integer maxStorageMb;
    private String billingEmail;
    private String billingCycle;
    private LocalDate nextBillingDate;
    private BigDecimal monthlyRate;
    private LocalDate trialEndsAt;
    private Boolean onboardingCompleted;
}
