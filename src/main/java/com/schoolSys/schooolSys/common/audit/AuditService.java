package com.schoolSys.schooolSys.common.audit;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Service responsible for recording and querying audit log entries.
 * <p>
 * Username is auto-captured from the Spring Security context and the client
 * IP is resolved from the current HTTP request (supports X-Forwarded-For).
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository repository;

    /* ── Write ──────────────────────────────────────────────── */

    /**
     * Records an audit event.
     *
     * @param action     the action (CREATE, UPDATE, DELETE, LOGIN, LOGOUT ...)
     * @param entityType the type of entity affected (STUDENT, TEACHER ...)
     * @param entityId   primary key of the affected entity (nullable)
     * @param details    JSON-formatted change details (nullable)
     */
    @Transactional
    public void log(String action, String entityType, UUID entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .username(currentUsername())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(currentIpAddress())
                .timestamp(LocalDateTime.now())
                .build();

        repository.save(entry);
        log.debug("Audit: {} {} {}#{} by {}", action, entityType, entityId, entry.getUsername());
    }

    /**
     * Convenience overload without entity info (e.g. LOGIN / LOGOUT events).
     */
    @Transactional
    public void log(String action, String details) {
        log(action, null, null, details);
    }

    /**
     * Records an authentication event (LOGIN_SUCCESS, LOGIN_FAILED,
     * ACCOUNT_LOCKED, LOGIN_2FA_REQUIRED, LOGOUT ...).
     * <p>
     * Runs in its own transaction ({@code REQUIRES_NEW}) so the entry is
     * persisted even when the surrounding {@code login()} transaction rolls
     * back after a failed attempt. Username and IP are supplied explicitly
     * because the actor is not yet authenticated at login time.
     * </p>
     *
     * @param action    the auth action
     * @param username  the email used for the attempt
     * @param ipAddress client IP, as resolved by the caller
     * @param details   human-readable context
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuth(String action, String username, String ipAddress, String details) {
        AuditLog entry = AuditLog.builder()
                .username(username)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();

        repository.save(entry);
        log.debug("Audit auth: {} by {} from {}", action, username, ipAddress);
    }

    /* ── Read ───────────────────────────────────────────────── */

    public Page<AuditLogDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(AuditLogDTO::fromEntity);
    }

    public Page<AuditLogDTO> findByUsername(String username, Pageable pageable) {
        return repository.findByUsername(username, pageable).map(AuditLogDTO::fromEntity);
    }

    public Page<AuditLogDTO> findByEntityType(String entityType, Pageable pageable) {
        return repository.findByEntityType(entityType, pageable).map(AuditLogDTO::fromEntity);
    }

    public Page<AuditLogDTO> findByDateRange(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return repository.findByDateRange(from, to, pageable).map(AuditLogDTO::fromEntity);
    }

    /**
     * Combined filter query. Any parameter can be {@code null} to skip that criterion.
     */
    public Page<AuditLogDTO> findFiltered(String username, String action, String entityType,
                                          LocalDateTime from, LocalDateTime to,
                                          Pageable pageable) {
        return repository.findFiltered(username, action, entityType, from, to, pageable)
                .map(AuditLogDTO::fromEntity);
    }

    /* ── Helpers ────────────────────────────────────────────── */

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getPrincipal().toString();
        }
        return "anonymous";
    }

    private String currentIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null && !xff.isBlank()) {
                    return xff.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Could not resolve client IP: {}", e.getMessage());
        }
        return "unknown";
    }
}
