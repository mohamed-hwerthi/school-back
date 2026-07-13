package com.schoolSys.schooolSys.disponibilite;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.disponibilite.dto.EnseignantDisponibiliteRequestDTO;
import com.schoolSys.schooolSys.disponibilite.dto.EnseignantDisponibiliteResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disponibilites-enseignants")
@RequiredArgsConstructor
public class EnseignantDisponibiliteController {

    private final EnseignantDisponibiliteService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnseignantDisponibiliteResponseDTO>>> getAll(
            @RequestParam(required = false) UUID enseignantId) {
        List<EnseignantDisponibiliteResponseDTO> data = enseignantId != null
                ? service.findByEnseignant(enseignantId)
                : service.findAll();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnseignantDisponibiliteResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<EnseignantDisponibiliteResponseDTO>> create(
            @Valid @RequestBody EnseignantDisponibiliteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<EnseignantDisponibiliteResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EnseignantDisponibiliteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
