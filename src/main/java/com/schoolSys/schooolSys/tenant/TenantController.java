package com.schoolSys.schooolSys.tenant;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.tenant.dto.TenantRequestDTO;
import com.schoolSys.schooolSys.tenant.dto.TenantResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing school tenants.
 * <p>
 * Every endpoint requires the MANAGE_TENANTS permission — tenant data must
 * never be enumerable by a regular authenticated user.
 * </p>
 */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * Lists all registered schools.
     *
     * @return all tenants
     */
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<List<TenantResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.findAll()));
    }

    /**
     * Gets a single tenant by ID.
     *
     * @param id the tenant ID
     * @return the tenant
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<TenantResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.findById(id)));
    }

    /**
     * Registers a new school and creates its dedicated PostgreSQL schema.
     *
     * @param dto the tenant creation request
     * @return the created tenant
     */
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<TenantResponseDTO>> create(@RequestBody TenantRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(tenantService.create(dto)));
    }

    /**
     * Updates a tenant's metadata.
     *
     * @param id  the tenant ID
     * @param dto the update request
     * @return the updated tenant
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<ApiResponse<TenantResponseDTO>> update(@PathVariable UUID id,
                                                                  @RequestBody TenantRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(tenantService.update(id, dto)));
    }

    /**
     * Deactivates a school (soft delete -- the schema is preserved).
     *
     * @param id the tenant ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TENANTS')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        tenantService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
