package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.finance.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caisse")
@RequiredArgsConstructor
public class CaisseController {

    private final CaisseService caisseService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<CaisseResponseDTO>>> getAll(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(caisseService.findAll(anneeScolaire)));
    }

    @GetMapping("/ouverte")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<CaisseResponseDTO>> getOuverte(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(caisseService.findOuverte(anneeScolaire)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<CaisseResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(caisseService.findById(id)));
    }

    @PostMapping("/ouvrir")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<CaisseResponseDTO>> ouvrir(
            @Valid @RequestBody CaisseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(caisseService.ouvrir(dto)));
    }

    @PatchMapping("/{id}/fermer")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<CaisseResponseDTO>> fermer(
            @PathVariable UUID id,
            @RequestParam(required = false) String fermePar) {
        return ResponseEntity.ok(ApiResponse.ok(caisseService.fermer(id, fermePar)));
    }

    // ── Mouvements ──

    @GetMapping("/{caisseId}/mouvements")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<MouvementCaisseResponseDTO>>> getMouvements(
            @PathVariable UUID caisseId) {
        return ResponseEntity.ok(ApiResponse.ok(caisseService.findMouvements(caisseId)));
    }

    @PostMapping("/mouvements")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<MouvementCaisseResponseDTO>> addMouvement(
            @Valid @RequestBody MouvementCaisseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(caisseService.addMouvement(dto)));
    }

    @DeleteMapping("/mouvements/{mouvementId}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMouvement(@PathVariable UUID mouvementId) {
        caisseService.deleteMouvement(mouvementId);
    }
}
