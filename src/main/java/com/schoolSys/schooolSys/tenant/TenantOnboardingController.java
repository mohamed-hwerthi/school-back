package com.schoolSys.schooolSys.tenant;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.tenant.dto.TenantOnboardingRequest;
import com.schoolSys.schooolSys.tenant.dto.TenantResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class TenantOnboardingController {

    private final TenantOnboardingService tenantOnboardingService;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponseDTO>> onboard(@Valid @RequestBody TenantOnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(tenantOnboardingService.onboard(request)));
    }
}
