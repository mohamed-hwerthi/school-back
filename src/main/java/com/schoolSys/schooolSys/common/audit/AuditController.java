package com.schoolSys.schooolSys.common.audit;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for querying the audit log.
 * All endpoints require the {@code READ_AUDIT} permission.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * Lists audit log entries with optional filters.
     *
     * <p>Query parameters:
     * <ul>
     *   <li>{@code username} — filter by actor username</li>
     *   <li>{@code action} — filter by action (CREATE, UPDATE, DELETE, LOGIN, LOGOUT)</li>
     *   <li>{@code entityType} — filter by entity type (STUDENT, TEACHER ...)</li>
     *   <li>{@code from} — start of date range (ISO date-time)</li>
     *   <li>{@code to} — end of date range (ISO date-time)</li>
     *   <li>{@code page}, {@code size}, {@code sort} — pagination</li>
     * </ul>
     */
    @GetMapping
    @PreAuthorize("hasAuthority('READ_AUDIT')")
    public ApiResponse<Page<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogDTO> result = auditService.findFiltered(username, action, entityType, from, to, pageable);
        return ApiResponse.ok(result);
    }
}
