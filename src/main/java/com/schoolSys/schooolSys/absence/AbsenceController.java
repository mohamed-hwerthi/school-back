package com.schoolSys.schooolSys.absence;

import java.util.UUID;

import com.schoolSys.schooolSys.absence.dto.*;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceService absenceService;

    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('WRITE_ABSENCES')")
    public ResponseEntity<ApiResponse<List<AbsenceResponseDTO>>> batchCreate(
            @Valid @RequestBody AbsenceBatchRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(absenceService.batchCreate(request)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<List<AbsenceResponseDTO>>> getByClasseAndDate(
            @RequestParam(required = false) UUID classeId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.getByClasseAndDate(classeId, date, type)));
    }

    @GetMapping("/feuilles")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<List<FeuilleJourDTO>>> getFeuillesByDate(
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.getFeuillesByDate(date)));
    }

    @GetMapping("/eleve/{eleveId}")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<List<AbsenceResponseDTO>>> getByEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.getByEleve(eleveId)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<AbsenceStatsDTO>> getStats(
            @RequestParam UUID classeId,
            @RequestParam int mois,
            @RequestParam int annee) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.getStats(classeId, mois, annee)));
    }

    @PutMapping("/{id}/justifier")
    @PreAuthorize("hasAuthority('WRITE_ABSENCES')")
    public ResponseEntity<ApiResponse<AbsenceResponseDTO>> justifier(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.justifier(id)));
    }

    /**
     * ABS-008: Submit justification with motif and optional file upload.
     */
    @PostMapping("/{id}/justifier")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<AbsenceResponseDTO>> submitJustification(
            @PathVariable UUID id,
            @RequestBody JustificationRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.submitJustification(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_ABSENCES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        absenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- ABS-006: Settings ----

    @GetMapping("/settings")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<AbsenceSettingsDTO>> getSettings() {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.getAbsenceSettings()));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('WRITE_ABSENCES')")
    public ResponseEntity<ApiResponse<AbsenceSettingsDTO>> updateSettings(
            @RequestBody AbsenceSettingsDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.updateAbsenceSettings(dto)));
    }

    // ---- ABS-007: Rapport mensuel ----

    @GetMapping("/rapport")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<RapportPresenceDTO>> getRapport(
            @RequestParam UUID classeId,
            @RequestParam int mois,
            @RequestParam int annee) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.getRapportMensuel(classeId, mois, annee)));
    }

    // ---- ABS-009: Historique presence par eleve ----

    @GetMapping("/historique/{eleveId}")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<ApiResponse<HistoriquePresenceDTO>> getHistorique(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(absenceService.getHistorique(eleveId)));
    }
}
