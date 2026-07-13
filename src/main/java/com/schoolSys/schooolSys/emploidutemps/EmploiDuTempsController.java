package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.emploidutemps.dto.*;
import com.schoolSys.schooolSys.emploidutemps.solver.TimetableSolverService;
import com.schoolSys.schooolSys.teacher.Teacher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;
    private final TimetableSolverService timetableSolverService;
    private final CurrentUserContext currentUser;

    // --- Emploi du temps endpoints ---

    @GetMapping("/api/emploi-du-temps/classe/{classeId}")
    @PreAuthorize("hasAuthority('READ_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<List<EmploiDuTempsResponseDTO>>> getByClasse(@PathVariable UUID classeId) {
        // A teacher may only read the timetable of a class he is affected to.
        if (currentUser.hasRole(UserRole.ENSEIGNANT) && !currentUser.teacherTeachesClasse(classeId)) {
            throw new AccessDeniedException("Cette classe n'est pas dans votre périmètre.");
        }
        return ResponseEntity.ok(ApiResponse.ok(emploiDuTempsService.getByClasse(classeId)));
    }

    @GetMapping("/api/emploi-du-temps/enseignant/{enseignantId}")
    @PreAuthorize("hasAuthority('READ_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<List<EmploiDuTempsResponseDTO>>> getByEnseignant(@PathVariable UUID enseignantId) {
        // A teacher may only read his own timetable.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            UUID ownId = currentUser.getCurrentTeacher().map(Teacher::getId).orElse(null);
            if (!Objects.equals(ownId, enseignantId)) {
                throw new AccessDeniedException("Vous ne pouvez consulter que votre propre emploi du temps.");
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(emploiDuTempsService.getByEnseignant(enseignantId)));
    }

    /** Current teacher's own timetable — resolves the teacher from the token, no id needed. */
    @GetMapping("/api/emploi-du-temps/me")
    @PreAuthorize("hasAuthority('READ_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<List<EmploiDuTempsResponseDTO>>> getMine() {
        List<EmploiDuTempsResponseDTO> result = currentUser.getCurrentTeacher()
                .map(t -> emploiDuTempsService.getByEnseignant(t.getId()))
                .orElseGet(List::of);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PutMapping("/api/emploi-du-temps/classe/{classeId}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<List<EmploiDuTempsResponseDTO>>> saveAll(
            @PathVariable UUID classeId,
            @Valid @RequestBody List<EmploiDuTempsRequestDTO> requests) {
        return ResponseEntity.ok(ApiResponse.ok(emploiDuTempsService.saveAll(classeId, requests)));
    }

    @PostMapping("/api/emploi-du-temps/check-conflits")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<List<ConflitDTO>>> checkConflits(
            @Valid @RequestBody List<EmploiDuTempsRequestDTO> requests) {
        return ResponseEntity.ok(ApiResponse.ok(emploiDuTempsService.detectConflits(requests)));
    }

    // --- Generation automatique ---

    @PostMapping("/api/emploi-du-temps/generate")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<TimetableGenerationResponseDTO>> generate(
            @Valid @RequestBody TimetableGenerationRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(timetableSolverService.generate(request)));
    }

    @GetMapping("/api/emploi-du-temps/preview-check")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<TimetablePreviewCheckDTO>> previewCheck(
            @RequestParam(required = false) UUID niveauId,
            @RequestParam(required = false) UUID anneeScolaireId) {
        return ResponseEntity.ok(ApiResponse.ok(
            timetableSolverService.previewCheck(niveauId, anneeScolaireId)));
    }

    // --- Creneau endpoints ---

    @GetMapping("/api/creneaux")
    @PreAuthorize("hasAuthority('READ_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<List<CreneauDTO>>> getAllCreneaux() {
        return ResponseEntity.ok(ApiResponse.ok(emploiDuTempsService.getAllCreneaux()));
    }

    @PostMapping("/api/creneaux")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<CreneauDTO>> createCreneau(@Valid @RequestBody CreneauDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(emploiDuTempsService.createCreneau(dto)));
    }

    @DeleteMapping("/api/creneaux/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<Void> deleteCreneau(@PathVariable UUID id) {
        emploiDuTempsService.deleteCreneau(id);
        return ResponseEntity.noContent().build();
    }

    // --- Remplacements ---

    @PostMapping("/api/emploi-du-temps/remplacements")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<RemplacementResponseDTO>> createRemplacement(
            @Valid @RequestBody RemplacementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(emploiDuTempsService.createRemplacement(dto)));
    }

    @GetMapping("/api/emploi-du-temps/remplacements")
    @PreAuthorize("hasAuthority('READ_EMPLOI_DU_TEMPS')")
    public ResponseEntity<ApiResponse<List<RemplacementResponseDTO>>> getRemplacements() {
        return ResponseEntity.ok(ApiResponse.ok(emploiDuTempsService.getAllRemplacements()));
    }

    @DeleteMapping("/api/emploi-du-temps/remplacements/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOI_DU_TEMPS')")
    public ResponseEntity<Void> deleteRemplacement(@PathVariable UUID id) {
        emploiDuTempsService.deleteRemplacement(id);
        return ResponseEntity.noContent().build();
    }
}
