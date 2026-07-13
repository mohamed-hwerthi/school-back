package com.schoolSys.schooolSys.auth;

/**
 * All fine-grained permissions used by the role-based access-control system.
 * Each enum constant maps to a Spring Security {@code GrantedAuthority}.
 */
public enum Permission {

    // ── Students ──────────────────────────────────────────────
    READ_STUDENTS,
    WRITE_STUDENTS,
    DELETE_STUDENTS,

    // ── Teachers ──────────────────────────────────────────────
    READ_TEACHERS,
    WRITE_TEACHERS,
    DELETE_TEACHERS,

    // ── Notes / Examens ───────────────────────────────────────
    READ_NOTES,
    WRITE_NOTES,

    // ── Absences ──────────────────────────────────────────────
    READ_ABSENCES,
    WRITE_ABSENCES,

    // ── Finance ───────────────────────────────────────────────
    READ_FINANCE,
    WRITE_FINANCE,

    // ── Emploi du temps ───────────────────────────────────────
    READ_EMPLOI_DU_TEMPS,
    WRITE_EMPLOI_DU_TEMPS,

    // ── Discipline ────────────────────────────────────────────
    READ_DISCIPLINE,
    WRITE_DISCIPLINE,

    // ── Messaging ─────────────────────────────────────────────
    READ_MESSAGES,
    WRITE_MESSAGES,

    // ── Bulletins ─────────────────────────────────────────────
    READ_BULLETINS,
    GENERATE_BULLETINS,

    // ── User & role management ────────────────────────────────
    MANAGE_USERS,
    MANAGE_ROLES,

    // ── School settings ───────────────────────────────────────
    MANAGE_SETTINGS,
    MANAGE_SCHOOL,

    // ── Audit ─────────────────────────────────────────────────
    READ_AUDIT,

    // ── Academic year ─────────────────────────────────────────
    MANAGE_ANNEE_SCOLAIRE,

    // ── Reports ───────────────────────────────────────────────
    READ_RAPPORTS,

    // ── HR (contrats, conges) ─────────────────────────────────
    MANAGE_RH,

    // ── Invoicing ─────────────────────────────────────────────
    MANAGE_FACTURES,

    // ── Multi-tenancy ─────────────────────────────────────────
    MANAGE_TENANTS,

    // ── Teacher management ──────────────────────────────────
    MANAGE_TEACHERS,

    // ── System administration ───────────────────────────────
    MANAGE_SYSTEM,

    // ── Reporting / Analytics ───────────────────────────────
    VIEW_REPORTS,

    // ── Communication ───────────────────────────────────────
    MANAGE_COMMUNICATION,

    // ── Parent portal ───────────────────────────────────────
    PARENT_ACCESS,

    // ── Inscriptions ──────────────────────────────────────
    MANAGE_INSCRIPTIONS,

    // ── Bibliothèque ──────────────────────────────────────
    MANAGE_BIBLIOTHEQUE,

    // ── Transport scolaire ────────────────────────────────
    MANAGE_TRANSPORT,

    // ── Cantine scolaire ──────────────────────────────────
    MANAGE_CANTINE,

    // ── Devoirs & Ressources ──────────────────────────────
    MANAGE_DEVOIRS,

    // ── Examens en ligne ──────────────────────────────────
    MANAGE_QUIZ
}
