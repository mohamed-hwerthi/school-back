package com.schoolSys.schooolSys.auth;

import java.util.*;

import static com.schoolSys.schooolSys.auth.Permission.*;

/**
 * Static mapping of each {@link UserRole} to its granted {@link Permission}s.
 * <p>
 * This is the single source of truth for the permission matrix (SEC-010).
 * </p>
 */
public final class RolePermissions {

    private static final Map<UserRole, Set<Permission>> MATRIX;

    static {
        Map<UserRole, Set<Permission>> m = new EnumMap<>(UserRole.class);

        // ── SUPER_ADMIN: every permission ──────────────────────
        m.put(UserRole.SUPER_ADMIN, EnumSet.allOf(Permission.class));

        // ── ADMIN: everything except tenant management ─────────
        Set<Permission> admin = EnumSet.allOf(Permission.class);
        admin.remove(MANAGE_TENANTS);
        m.put(UserRole.ADMIN, admin);

        // ── DIRECTEUR ──────────────────────────────────────────
        m.put(UserRole.DIRECTEUR, EnumSet.of(
                READ_STUDENTS, WRITE_STUDENTS, DELETE_STUDENTS,
                READ_TEACHERS, WRITE_TEACHERS, DELETE_TEACHERS,
                MANAGE_TEACHERS,
                READ_NOTES, WRITE_NOTES,
                READ_ABSENCES, WRITE_ABSENCES,
                READ_FINANCE, WRITE_FINANCE,
                READ_EMPLOI_DU_TEMPS, WRITE_EMPLOI_DU_TEMPS,
                READ_DISCIPLINE, WRITE_DISCIPLINE,
                READ_MESSAGES, WRITE_MESSAGES,
                READ_BULLETINS, GENERATE_BULLETINS,
                MANAGE_ANNEE_SCOLAIRE,
                READ_AUDIT,
                READ_RAPPORTS,
                VIEW_REPORTS,
                MANAGE_SETTINGS,
                MANAGE_COMMUNICATION,
                MANAGE_INSCRIPTIONS,
                MANAGE_BIBLIOTHEQUE,
                MANAGE_TRANSPORT,
                MANAGE_CANTINE,
                MANAGE_DEVOIRS,
                MANAGE_QUIZ,
                MANAGE_RH,
                MANAGE_USERS
        ));

        // ── ENSEIGNANT ─────────────────────────────────────────
        m.put(UserRole.ENSEIGNANT, EnumSet.of(
                READ_STUDENTS,
                WRITE_NOTES, READ_NOTES,
                READ_ABSENCES, WRITE_ABSENCES,
                READ_EMPLOI_DU_TEMPS,
                READ_DISCIPLINE,
                READ_MESSAGES, WRITE_MESSAGES,
                READ_BULLETINS,
                MANAGE_DEVOIRS,
                MANAGE_QUIZ
        ));

        // ── COMPTABLE ──────────────────────────────────────────
        m.put(UserRole.COMPTABLE, EnumSet.of(
                READ_STUDENTS,
                READ_FINANCE, WRITE_FINANCE,
                MANAGE_FACTURES,
                READ_RAPPORTS
        ));

        // ── PARENT ─────────────────────────────────────────────
        m.put(UserRole.PARENT, EnumSet.of(
                READ_STUDENTS,
                READ_NOTES,
                READ_ABSENCES,
                READ_EMPLOI_DU_TEMPS,
                READ_MESSAGES, WRITE_MESSAGES,
                READ_BULLETINS,
                PARENT_ACCESS
        ));

        MATRIX = Collections.unmodifiableMap(m);
    }

    private RolePermissions() { /* utility class */ }

    /**
     * Returns the immutable set of permissions granted to the given role.
     */
    public static Set<Permission> getPermissions(UserRole role) {
        return MATRIX.getOrDefault(role, Collections.emptySet());
    }
}
