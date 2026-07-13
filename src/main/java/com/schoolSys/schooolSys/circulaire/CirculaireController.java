package com.schoolSys.schooolSys.circulaire;

import java.util.UUID;

import com.schoolSys.schooolSys.circulaire.dto.CirculaireRequestDTO;
import com.schoolSys.schooolSys.circulaire.dto.CirculaireResponseDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/circulaires")
@RequiredArgsConstructor
public class CirculaireController {

    private final CirculaireService circulaireService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CirculaireResponseDTO>>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String cible,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(circulaireService.findAll(type, statut, cible, search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CirculaireResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(circulaireService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<CirculaireResponseDTO>> create(
            @Valid @RequestBody CirculaireRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(circulaireService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<CirculaireResponseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody CirculaireRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(circulaireService.update(id, dto)));
    }

    @PutMapping("/{id}/publier")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<CirculaireResponseDTO>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(circulaireService.publish(id)));
    }

    @PutMapping("/{id}/archiver")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<CirculaireResponseDTO>> archive(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(circulaireService.archive(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        circulaireService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
