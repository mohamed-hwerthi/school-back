package com.schoolSys.schooolSys.tenant;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.tenant.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final TenantBillingService tenantBillingService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<SuperAdminDashboardDTO>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(superAdminService.getDashboard()));
    }

    @GetMapping("/tenants")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<List<TenantResponseDTO>>> getAllTenants() {
        return ResponseEntity.ok(ApiResponse.ok(superAdminService.getAllTenants()));
    }

    @GetMapping("/tenants/{id}/usage")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<TenantUsageDTO>> getTenantUsage(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(superAdminService.getTenantUsage(id)));
    }

    @PutMapping("/tenants/{id}/plan")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<TenantPlanDTO>> changePlan(
            @PathVariable UUID id, @RequestParam String plan) {
        return ResponseEntity.ok(ApiResponse.ok(tenantBillingService.changePlan(id, plan)));
    }

    @PutMapping("/tenants/{id}/activate")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<Void> activateTenant(@PathVariable UUID id) {
        superAdminService.activateTenant(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tenants/{id}/deactivate")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<Void> deactivateTenant(@PathVariable UUID id) {
        superAdminService.deactivateTenant(id);
        return ResponseEntity.noContent().build();
    }
}
