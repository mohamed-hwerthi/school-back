package com.schoolSys.schooolSys.module;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.module.dto.ModuleRequestDTO;
import com.schoolSys.schooolSys.module.dto.ModuleResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ModuleResponseDTO>>> getAll(
            @RequestParam(required = false) UUID niveauId) {
        return ResponseEntity.ok(ApiResponse.ok(moduleService.findAll(niveauId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuleResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(moduleService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<ModuleResponseDTO>> create(
            @Valid @RequestBody ModuleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(moduleService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<ModuleResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ModuleRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(moduleService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
