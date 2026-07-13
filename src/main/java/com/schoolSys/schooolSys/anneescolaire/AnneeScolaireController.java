package com.schoolSys.schooolSys.anneescolaire;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.dto.*;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annees-scolaires")
@RequiredArgsConstructor
public class AnneeScolaireController {

    private final AnneeScolaireService anneeScolaireService;

    // --- AnneeScolaire endpoints ---

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnneeScolaireResponseDTO>>> getAllAnnees() {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.getAllAnnees()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnneeScolaireResponseDTO>> getAnneeById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.getAnneeById(id)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<AnneeScolaireResponseDTO>> getActiveAnnee() {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.getActiveAnnee()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<AnneeScolaireResponseDTO>> createAnnee(
            @Valid @RequestBody AnneeScolaireRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(anneeScolaireService.createAnnee(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<AnneeScolaireResponseDTO>> updateAnnee(
            @PathVariable UUID id,
            @Valid @RequestBody AnneeScolaireRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.updateAnnee(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<Void> deleteAnnee(@PathVariable UUID id) {
        anneeScolaireService.deleteAnnee(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}/activate", method = { RequestMethod.PUT, RequestMethod.PATCH })
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<AnneeScolaireResponseDTO>> activateAnnee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.activateAnnee(id)));
    }

    @RequestMapping(value = "/{id}/cloturer", method = { RequestMethod.PUT, RequestMethod.PATCH })
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<AnneeScolaireResponseDTO>> cloturerAnnee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.cloturerAnnee(id)));
    }

    // --- Trimestre endpoints ---

    @GetMapping("/{anneeScolaireId}/trimestres")
    public ResponseEntity<ApiResponse<List<TrimestreDTO>>> getTrimestres(@PathVariable UUID anneeScolaireId) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.getTrimestresByAnnee(anneeScolaireId)));
    }

    @PostMapping("/{anneeScolaireId}/trimestres")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<TrimestreDTO>> addTrimestre(
            @PathVariable UUID anneeScolaireId,
            @Valid @RequestBody TrimestreDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(anneeScolaireService.addTrimestre(anneeScolaireId, dto)));
    }

    @PutMapping("/trimestres/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<TrimestreDTO>> updateTrimestre(
            @PathVariable UUID id,
            @Valid @RequestBody TrimestreDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.updateTrimestre(id, dto)));
    }

    @DeleteMapping("/trimestres/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<Void> deleteTrimestre(@PathVariable UUID id) {
        anneeScolaireService.deleteTrimestre(id);
        return ResponseEntity.noContent().build();
    }

    // --- Vacance endpoints ---

    @GetMapping("/{anneeScolaireId}/vacances")
    public ResponseEntity<ApiResponse<List<VacanceDTO>>> getVacances(@PathVariable UUID anneeScolaireId) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.getVacancesByAnnee(anneeScolaireId)));
    }

    @PostMapping("/{anneeScolaireId}/vacances")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<VacanceDTO>> addVacance(
            @PathVariable UUID anneeScolaireId,
            @Valid @RequestBody VacanceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(anneeScolaireService.addVacance(anneeScolaireId, dto)));
    }

    @PutMapping("/vacances/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<VacanceDTO>> updateVacance(
            @PathVariable UUID id,
            @Valid @RequestBody VacanceDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.updateVacance(id, dto)));
    }

    @DeleteMapping("/vacances/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<Void> deleteVacance(@PathVariable UUID id) {
        anneeScolaireService.deleteVacance(id);
        return ResponseEntity.noContent().build();
    }

    // --- JourFerie endpoints ---

    @GetMapping("/{anneeScolaireId}/jours-feries")
    public ResponseEntity<ApiResponse<List<JourFerieDTO>>> getJoursFeries(@PathVariable UUID anneeScolaireId) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.getJoursFeriesByAnnee(anneeScolaireId)));
    }

    @PostMapping("/{anneeScolaireId}/jours-feries")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<JourFerieDTO>> addJourFerie(
            @PathVariable UUID anneeScolaireId,
            @Valid @RequestBody JourFerieDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(anneeScolaireService.addJourFerie(anneeScolaireId, dto)));
    }

    @PutMapping("/jours-feries/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<JourFerieDTO>> updateJourFerie(
            @PathVariable UUID id,
            @Valid @RequestBody JourFerieDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(anneeScolaireService.updateJourFerie(id, dto)));
    }

    @DeleteMapping("/jours-feries/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<Void> deleteJourFerie(@PathVariable UUID id) {
        anneeScolaireService.deleteJourFerie(id);
        return ResponseEntity.noContent().build();
    }
}
