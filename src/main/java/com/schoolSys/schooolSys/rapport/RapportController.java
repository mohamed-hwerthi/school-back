package com.schoolSys.schooolSys.rapport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.rapport.dto.RapportRequestDTO;
import com.schoolSys.schooolSys.rapport.dto.RapportResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_RAPPORTS')")
    public ResponseEntity<ApiResponse<List<RapportResponseDTO>>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(ApiResponse.ok(rapportService.findAll(type, statut)));
    }

    // Regex {id:\\d+} : empêche ce mapping de capturer les sous-chemins
    // non-numériques (ex: /rapports/dashboard) qui doivent donner 404, pas 500.
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('READ_RAPPORTS')")
    public ResponseEntity<ApiResponse<RapportResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(rapportService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<RapportResponseDTO>> create(
            @Valid @RequestBody RapportRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(rapportService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<RapportResponseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody RapportRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(rapportService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        rapportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
