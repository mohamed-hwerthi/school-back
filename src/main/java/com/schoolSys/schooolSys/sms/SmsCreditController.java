package com.schoolSys.schooolSys.sms;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SmsCreditController {

    private final SmsCreditService smsCreditService;

    @GetMapping("/api/sms/credits")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyCredits() {
        String tenantId = TenantContext.getCurrentTenant();
        SchoolSmsCredit credits = smsCreditService.getCredits(tenantId);
        Map<String, Object> response = Map.of(
                "tenantId", credits.getTenantId(),
                "totalCredits", credits.getTotalCredits(),
                "usedCredits", credits.getUsedCredits(),
                "remaining", credits.getRemaining()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/super-admin/sms-credits")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<List<SchoolSmsCredit>>> getAllCredits() {
        return ResponseEntity.ok(ApiResponse.ok(smsCreditService.getAllCredits()));
    }

    @GetMapping("/api/super-admin/sms-credits/{tenantId}")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<SchoolSmsCredit>> getTenantCredits(@PathVariable String tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(smsCreditService.getCredits(tenantId)));
    }

    @PutMapping("/api/super-admin/sms-credits/{tenantId}")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<SchoolSmsCredit>> setTenantCredits(
            @PathVariable String tenantId,
            @RequestBody Map<String, Integer> body) {
        int totalCredits = body.getOrDefault("totalCredits", 0);
        return ResponseEntity.ok(ApiResponse.ok(smsCreditService.setCredits(tenantId, totalCredits)));
    }

    @PatchMapping("/api/super-admin/sms-credits/{tenantId}/add")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<SchoolSmsCredit>> addTenantCredits(
            @PathVariable String tenantId,
            @RequestBody Map<String, Integer> body) {
        int additional = body.getOrDefault("additionalCredits", 0);
        return ResponseEntity.ok(ApiResponse.ok(smsCreditService.addCredits(tenantId, additional)));
    }
}
