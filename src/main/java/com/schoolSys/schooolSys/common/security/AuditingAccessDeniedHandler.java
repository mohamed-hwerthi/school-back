package com.schoolSys.schooolSys.common.security;

import com.schoolSys.schooolSys.common.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logs every authorization failure to the audit_logs table with action="ACCESS_DENIED",
 * then defers to the default 403 response. Allows admins to spot
 * permission-matrix gaps and probing attempts via /dashboard/tracabilite.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditingAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditService auditService;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        try {
            String details = String.format(
                    "{\"endpoint\":\"%s %s\",\"reason\":\"%s\",\"ip\":\"%s\"}",
                    request.getMethod(),
                    request.getRequestURI(),
                    safe(accessDeniedException.getMessage()),
                    safe(extractIp(request))
            );
            auditService.log("ACCESS_DENIED", details);
        } catch (Exception e) {
            // Never let audit logging break the response.
            log.warn("Failed to record ACCESS_DENIED audit entry", e);
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Vous n'avez pas l'autorisation d'accéder à cette ressource.");
    }

    private static String extractIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }
}
