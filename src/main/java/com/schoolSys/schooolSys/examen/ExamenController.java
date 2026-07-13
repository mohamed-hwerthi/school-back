package com.schoolSys.schooolSys.examen;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.examen.dto.ExamenRequestDTO;
import com.schoolSys.schooolSys.examen.dto.ExamenResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examens")
@RequiredArgsConstructor
public class ExamenController {

    private final ExamenService examenService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<ExamenResponseDTO>>> getAll(
            @RequestParam(required = false) UUID moduleId,
            @RequestParam(required = false) UUID classeId,
            @RequestParam(required = false) Integer trimestre,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(examenService.findAll(moduleId, classeId, trimestre, anneeScolaire)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<ExamenResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(examenService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<ExamenResponseDTO>> create(
            @Valid @RequestBody ExamenRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(examenService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<ExamenResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ExamenRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(examenService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        examenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<UUID> ids) {
        examenService.deleteBulk(ids);
        return ResponseEntity.noContent().build();
    }
}
