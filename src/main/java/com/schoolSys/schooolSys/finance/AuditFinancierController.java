package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.finance.dto.AuditFinancierDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-financier")
@RequiredArgsConstructor
public class AuditFinancierController {

    private final AuditFinancierService auditFinancierService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<AuditFinancierDTO>>> getAll(
            @RequestParam(required = false) String entityType) {
        return ResponseEntity.ok(ApiResponse.ok(auditFinancierService.getAll(entityType)));
    }

    @GetMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<AuditFinancierDTO>>> getByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {
        return ResponseEntity.ok(ApiResponse.ok(
                auditFinancierService.getByEntity(entityType, entityId)));
    }
}
