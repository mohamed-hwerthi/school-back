package com.schoolSys.schooolSys.domaine;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.domaine.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/domaines")
@RequiredArgsConstructor
public class DomaineController {

    private final DomaineService domaineService;

    // ── Domaines ────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<DomaineResponseDTO>>> getAll(
            @RequestParam(required = false) UUID niveauId) {
        return ResponseEntity.ok(ApiResponse.ok(domaineService.findAll(niveauId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DomaineResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(domaineService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<DomaineResponseDTO>> create(
            @Valid @RequestBody DomaineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(domaineService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<DomaineResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody DomaineRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(domaineService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        domaineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Sous-domaines ───────────────────────────────────────

    @GetMapping("/{domaineId}/sous-domaines")
    public ResponseEntity<ApiResponse<List<SousDomaineDTO>>> getSousDomaines(
            @PathVariable UUID domaineId) {
        return ResponseEntity.ok(ApiResponse.ok(domaineService.findSousDomaines(domaineId)));
    }

    @PostMapping("/sous-domaines")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<SousDomaineDTO>> createSousDomaine(
            @Valid @RequestBody SousDomaineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(domaineService.createSousDomaine(dto)));
    }

    @PutMapping("/sous-domaines/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<SousDomaineDTO>> updateSousDomaine(
            @PathVariable UUID id,
            @Valid @RequestBody SousDomaineRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(domaineService.updateSousDomaine(id, dto)));
    }

    @DeleteMapping("/sous-domaines/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<Void> deleteSousDomaine(@PathVariable UUID id) {
        domaineService.deleteSousDomaine(id);
        return ResponseEntity.noContent().build();
    }
}
