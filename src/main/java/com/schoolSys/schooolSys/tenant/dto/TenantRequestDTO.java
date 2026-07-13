package com.schoolSys.schooolSys.tenant.dto;

import lombok.Data;

/**
 * DTO for creating or updating a school tenant.
 */
@Data
public class TenantRequestDTO {

    /** Display name of the school. */
    private String name;

    /**
     * PostgreSQL schema name (lowercase, alphanumeric + underscores, 3-63 chars).
     * Only used during creation — updates ignore this field.
     */
    private String schemaName;

    /** Contact email for the school administrator. */
    private String contactEmail;

    /** URL-friendly slug for the public vitrine (optional — auto-generated from name if blank). */
    private String slug;

    /**
     * Email of the school's first ADMIN account, auto-created with the tenant.
     * Optional — if blank, no admin account is created.
     */
    private String adminEmail;

    /** Initial password for the auto-created school ADMIN (required if adminEmail is set). */
    private String adminPassword;
}
