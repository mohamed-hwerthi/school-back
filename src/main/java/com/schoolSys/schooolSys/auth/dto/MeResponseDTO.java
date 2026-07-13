package com.schoolSys.schooolSys.auth.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * Enriched payload for {@code GET /api/auth/me}.
 * Returns the user record plus everything the frontend needs to make
 * permission/role decisions and apply optimistic UI scoping
 * without re-querying the backend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponseDTO {

    /** Identity + role info. */
    private UserResponseDTO user;

    /** Flat list of granted Permission names (e.g. "READ_STUDENTS"). */
    private List<String> permissions;

    /** Class IDs the current user can see (empty for staff with full scope). */
    private Set<UUID> scopedClasseIds;

    /** Student IDs the current user can see (PARENT only; empty otherwise). */
    private Set<UUID> scopedStudentIds;
}
