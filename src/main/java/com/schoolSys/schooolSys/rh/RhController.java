package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.rh.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rh")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_RH')")
public class RhController {

    private final RhService rhService;

    // ── Contrats ──────────────────────────────────────────────

    @GetMapping("/contrats")
    public ResponseEntity<ApiResponse<List<ContratResponseDTO>>> getAllContrats() {
        return ResponseEntity.ok(ApiResponse.ok(rhService.getAllContrats()));
    }

    @GetMapping("/contrats/{id}")
    public ResponseEntity<ApiResponse<ContratResponseDTO>> getContratById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.getContratById(id)));
    }

    @GetMapping("/contrats/enseignant/{enseignantId}")
    public ResponseEntity<ApiResponse<List<ContratResponseDTO>>> getContratsByEnseignant(
            @PathVariable UUID enseignantId) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.getContratsByEnseignant(enseignantId)));
    }

    @PostMapping("/contrats")
    public ResponseEntity<ApiResponse<ContratResponseDTO>> createContrat(
            @Valid @RequestBody ContratRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(rhService.createContrat(dto)));
    }

    @PutMapping("/contrats/{id}")
    public ResponseEntity<ApiResponse<ContratResponseDTO>> updateContrat(
            @PathVariable UUID id, @Valid @RequestBody ContratRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.updateContrat(id, dto)));
    }

    @DeleteMapping("/contrats/{id}")
    public ResponseEntity<Void> deleteContrat(@PathVariable UUID id) {
        rhService.deleteContrat(id);
        return ResponseEntity.noContent().build();
    }

    // ── Conges ────────────────────────────────────────────────

    @GetMapping("/conges")
    public ResponseEntity<ApiResponse<List<CongeResponseDTO>>> getAllConges(
            @RequestParam(required = false) String statut) {
        List<CongeResponseDTO> result = statut != null
                ? rhService.getCongesByStatut(statut)
                : rhService.getAllConges();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/conges/{id}")
    public ResponseEntity<ApiResponse<CongeResponseDTO>> getCongeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.getCongeById(id)));
    }

    @GetMapping("/conges/enseignant/{enseignantId}")
    public ResponseEntity<ApiResponse<List<CongeResponseDTO>>> getCongesByEnseignant(
            @PathVariable UUID enseignantId) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.getCongesByEnseignant(enseignantId)));
    }

    @PostMapping("/conges")
    public ResponseEntity<ApiResponse<CongeResponseDTO>> createConge(
            @Valid @RequestBody CongeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(rhService.createConge(dto)));
    }

    @PutMapping("/conges/{id}")
    public ResponseEntity<ApiResponse<CongeResponseDTO>> updateConge(
            @PathVariable UUID id, @Valid @RequestBody CongeRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.updateConge(id, dto)));
    }

    @PutMapping("/conges/{id}/approuver")
    public ResponseEntity<ApiResponse<CongeResponseDTO>> approuverConge(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.approuverConge(id)));
    }

    @PutMapping("/conges/{id}/refuser")
    public ResponseEntity<ApiResponse<CongeResponseDTO>> refuserConge(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(rhService.refuserConge(id)));
    }

    @DeleteMapping("/conges/{id}")
    public ResponseEntity<Void> deleteConge(@PathVariable UUID id) {
        rhService.deleteConge(id);
        return ResponseEntity.noContent().build();
    }
}
