package com.schoolSys.schooolSys.tenant;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.tenant.dto.PublicSchoolDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public-facing API for discovering schools.
 * <p>
 * No authentication required — used by the mobile app's school picker so a
 * user can select their school before logging in. Unlike {@code /api/tenants}
 * (which requires {@code MANAGE_TENANTS}), this endpoint exposes only a minimal
 * projection ({@link PublicSchoolDTO}) of <em>active</em> schools.
 * </p>
 */
@RestController
@RequestMapping("/api/public/schools")
@RequiredArgsConstructor
public class PublicSchoolController {

    private final TenantService tenantService;

    /**
     * Lists all active schools for the public school picker.
     *
     * @return active schools (id, name, slug)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicSchoolDTO>>> getActiveSchools() {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.findActivePublicSchools()));
    }
}
