package com.schoolSys.schooolSys.personnel;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.personnel.dto.PersonnelRequestDTO;
import com.schoolSys.schooolSys.personnel.dto.PersonnelResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD endpoints for non-teaching staff. Guarded by {@code MANAGE_RH}, the same
 * authority used for the rest of the HR module (SUPER_ADMIN, ADMIN, DIRECTEUR).
 */
@RestController
@RequestMapping("/api/personnel")
@RequiredArgsConstructor
public class PersonnelController {

    private final PersonnelService personnelService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_RH')")
    public ResponseEntity<ApiResponse<List<PersonnelResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(personnelService.findAll()));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('MANAGE_RH')")
    public ResponseEntity<ApiResponse<PagedResponse<PersonnelResponseDTO>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fonction,
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(ApiResponse.ok(personnelService.findPage(search, fonction, statut, page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_RH')")
    public ResponseEntity<ApiResponse<PersonnelResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(personnelService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_RH')")
    public ResponseEntity<ApiResponse<PersonnelResponseDTO>> create(
            @Valid @RequestBody PersonnelRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(personnelService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_RH')")
    public ResponseEntity<ApiResponse<PersonnelResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PersonnelRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(personnelService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_RH')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        personnelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('MANAGE_RH')")
    public ResponseEntity<ApiResponse<List<PersonnelResponseDTO>>> importBulk(
            @RequestBody List<@Valid PersonnelRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(personnelService.importBulk(dtos)));
    }
}
