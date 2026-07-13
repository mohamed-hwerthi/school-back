package com.schoolSys.schooolSys.discipline;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.discipline.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discipline")
@RequiredArgsConstructor
public class DisciplineController {

    private final DisciplineService disciplineService;

    // --- Incident endpoints ---

    @GetMapping("/incidents")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<List<IncidentResponseDTO>>> getAllIncidents() {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getAllIncidents()));
    }

    @GetMapping("/incidents/{id}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<IncidentResponseDTO>> getIncidentById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getIncidentById(id)));
    }

    @GetMapping("/incidents/date-range")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<List<IncidentResponseDTO>>> getIncidentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getIncidentsByDateRange(start, end)));
    }

    @GetMapping("/incidents/type/{type}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<List<IncidentResponseDTO>>> getIncidentsByType(@PathVariable String type) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getIncidentsByType(type)));
    }

    @GetMapping("/incidents/gravite/{gravite}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<List<IncidentResponseDTO>>> getIncidentsByGravite(@PathVariable String gravite) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getIncidentsByGravite(gravite)));
    }

    @PostMapping("/incidents")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<ApiResponse<IncidentResponseDTO>> createIncident(
            @Valid @RequestBody IncidentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(disciplineService.createIncident(request)));
    }

    @PutMapping("/incidents/{id}")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<ApiResponse<IncidentResponseDTO>> updateIncident(
            @PathVariable UUID id,
            @Valid @RequestBody IncidentRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.updateIncident(id, request)));
    }

    @DeleteMapping("/incidents/{id}")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<Void> deleteIncident(@PathVariable UUID id) {
        disciplineService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }

    // --- Sanction endpoints ---

    @GetMapping("/sanctions")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<List<SanctionResponseDTO>>> getAllSanctions() {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getAllSanctions()));
    }

    @GetMapping("/sanctions/{id}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<SanctionResponseDTO>> getSanctionById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getSanctionById(id)));
    }

    @GetMapping("/sanctions/eleve/{eleveId}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<List<SanctionResponseDTO>>> getSanctionsByEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getSanctionsByEleve(eleveId)));
    }

    @GetMapping("/sanctions/incident/{incidentId}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<List<SanctionResponseDTO>>> getSanctionsByIncident(@PathVariable UUID incidentId) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getSanctionsByIncident(incidentId)));
    }

    @PostMapping("/sanctions")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<ApiResponse<SanctionResponseDTO>> createSanction(
            @Valid @RequestBody SanctionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(disciplineService.createSanction(request)));
    }

    @PutMapping("/sanctions/{id}")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<ApiResponse<SanctionResponseDTO>> updateSanction(
            @PathVariable UUID id,
            @Valid @RequestBody SanctionRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.updateSanction(id, request)));
    }

    @DeleteMapping("/sanctions/{id}")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<Void> deleteSanction(@PathVariable UUID id) {
        disciplineService.deleteSanction(id);
        return ResponseEntity.noContent().build();
    }

    // --- DISC-004: Workflow sanction endpoints ---

    @GetMapping("/dossier/{eleveId}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<DossierDisciplinaireDTO>> getDossierDisciplinaire(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.getDossierDisciplinaire(eleveId)));
    }

    @PostMapping("/sanctions/{id}/approuver")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<ApiResponse<SanctionResponseDTO>> approveSanction(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        UUID approvedBy = body.get("approuveParId") != null ? UUID.fromString(body.get("approuveParId").toString()) : null;
        String comment = body.get("commentaire") != null ? body.get("commentaire").toString() : null;
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.approveSanction(id, approvedBy, comment)));
    }

    @PostMapping("/sanctions/{id}/lever")
    @PreAuthorize("hasAuthority('WRITE_DISCIPLINE')")
    public ResponseEntity<ApiResponse<SanctionResponseDTO>> leverSanction(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.leverSanction(id)));
    }

    @GetMapping("/sanctions/suggestion/{eleveId}")
    @PreAuthorize("hasAuthority('READ_DISCIPLINE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSanctionSuggestion(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(disciplineService.escalateSanction(eleveId)));
    }
}
