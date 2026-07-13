package com.schoolSys.schooolSys.tenant;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a registered school (tenant) in the system.
 * <p>
 * Each tenant maps to a dedicated PostgreSQL schema identified by
 * {@link #schemaName}. This entity is always stored in the {@code public}
 * schema so it is accessible regardless of the current tenant context.
 * </p>
 */
@Entity
@Table(name = "tenants", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    /** Display name of the school. */
    @Column(nullable = false)
    private String name;

    /**
     * PostgreSQL schema name for this school.
     * Must be lowercase, alphanumeric with underscores only.
     */
    @Column(name = "schema_name", unique = true, nullable = false)
    private String schemaName;

    /** Contact email for the school administrator. */
    @Column(unique = true, nullable = false)
    private String contactEmail;

    /** URL-friendly slug for the public vitrine (e.g. "ecole-einstein"). */
    @Column(unique = true, nullable = false)
    private String slug;

    /** Whether the tenant is currently active. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── SaaS / Billing fields ──

    @Builder.Default
    @Column(length = 20)
    private String plan = "FREE";

    @Builder.Default
    @Column(name = "max_students")
    private Integer maxStudents = 50;

    @Builder.Default
    @Column(name = "max_teachers")
    private Integer maxTeachers = 10;

    @Builder.Default
    @Column(name = "max_storage_mb")
    private Integer maxStorageMb = 500;

    @Column(name = "billing_email", length = 200)
    private String billingEmail;

    @Builder.Default
    @Column(name = "billing_cycle", length = 20)
    private String billingCycle = "MONTHLY";

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

    @Builder.Default
    @Column(name = "monthly_rate", precision = 10, scale = 2)
    private BigDecimal monthlyRate = BigDecimal.ZERO;

    @Column(name = "trial_ends_at")
    private LocalDate trialEndsAt;

    @Builder.Default
    @Column(name = "onboarding_completed")
    private Boolean onboardingCompleted = false;
}
