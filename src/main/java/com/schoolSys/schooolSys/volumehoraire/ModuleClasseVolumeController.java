package com.schoolSys.schooolSys.volumehoraire;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.volumehoraire.dto.ModuleClasseVolumeRequestDTO;
import com.schoolSys.schooolSys.volumehoraire.dto.ModuleClasseVolumeResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volume-horaire")
@RequiredArgsConstructor
public class ModuleClasseVolumeController {

    private final ModuleClasseVolumeService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ModuleClasseVolumeResponseDTO>>> getAll(
            @RequestParam(required = false) UUID classeId,
            @RequestParam(required = false) UUID anneeScolaireId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findAll(classeId, anneeScolaireId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleClasseVolumeResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<ModuleClasseVolumeResponseDTO>> create(
            @Valid @RequestBody ModuleClasseVolumeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<ModuleClasseVolumeResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ModuleClasseVolumeRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
