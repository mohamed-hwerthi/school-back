package com.schoolSys.schooolSys.devoir;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.devoir.dto.CreateDevoirRequest;
import com.schoolSys.schooolSys.devoir.dto.DevoirDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devoirs")
@RequiredArgsConstructor
public class DevoirController {

    private final DevoirService devoirService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<DevoirDTO>>> getAll(
            @RequestParam(required = false) UUID classeId,
            @RequestParam(required = false) UUID moduleId,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(devoirService.findAll(classeId, moduleId, anneeScolaire)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<DevoirDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(devoirService.findById(id)));
    }

    @GetMapping("/classe/{classeId}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<DevoirDTO>>> getByClasse(@PathVariable UUID classeId) {
        return ResponseEntity.ok(ApiResponse.ok(devoirService.findByClasse(classeId)));
    }

    @GetMapping("/module/{moduleId}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<DevoirDTO>>> getByModule(@PathVariable UUID moduleId) {
        return ResponseEntity.ok(ApiResponse.ok(devoirService.findByModule(moduleId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<DevoirDTO>> create(@Valid @RequestBody CreateDevoirRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(devoirService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<DevoirDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateDevoirRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(devoirService.update(id, request)));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<DevoirDTO>> close(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(devoirService.closeDevoir(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        devoirService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
