package com.schoolSys.schooolSys.tenant.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * Minimal, safe-to-expose view of a school used by the public school picker
 * (mobile app) before authentication.
 * <p>
 * Deliberately omits contact email, billing data and quotas — only what is
 * needed to let a user recognise and select their school.
 * </p>
 */
@Data
@Builder
public class PublicSchoolDTO {

    /** Tenant identifier. */
    private UUID id;

    /** Human-readable school name. */
    private String name;

    /** URL-friendly slug identifying the school. */
    private String slug;
}
