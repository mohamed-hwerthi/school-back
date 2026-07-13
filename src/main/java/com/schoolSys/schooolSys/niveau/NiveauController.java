package com.schoolSys.schooolSys.niveau;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.niveau.dto.ClasseRequestDTO;
import com.schoolSys.schooolSys.niveau.dto.NiveauRequestDTO;
import com.schoolSys.schooolSys.niveau.dto.NiveauResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/niveaux")
@RequiredArgsConstructor
public class NiveauController {

    private final NiveauService niveauService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NiveauResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(niveauService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NiveauResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(niveauService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<NiveauResponseDTO>> create(
            @Valid @RequestBody NiveauRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(niveauService.create(dto.getName())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        niveauService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/classes")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<NiveauResponseDTO>> addClasse(
            @PathVariable UUID id,
            @Valid @RequestBody ClasseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(niveauService.addClasse(id, dto.getLetter())));
    }

    @DeleteMapping("/{id}/classes/{letter}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<Void> removeClasse(
            @PathVariable UUID id,
            @PathVariable String letter) {
        niveauService.removeClasse(id, letter);
        return ResponseEntity.noContent().build();
    }
}
