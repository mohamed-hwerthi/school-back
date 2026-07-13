package com.schoolSys.schooolSys.note;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.note.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<NoteResponseDTO>>> getByExamen(
            @RequestParam UUID examenId,
            @RequestParam Integer trimestre,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.findByExamen(examenId, trimestre, anneeScolaire)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<NoteResponseDTO>>> getByStudent(
            @PathVariable UUID studentId,
            @RequestParam Integer trimestre,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.findByStudent(studentId, trimestre, anneeScolaire)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<List<NoteResponseDTO>>> upsertBulk(@Valid @RequestBody BulkNoteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.upsertBulk(dto.getNotes())));
    }

    @GetMapping("/moyennes")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<MoyenneDTO>>> getMoyennes(
            @RequestParam UUID classeId,
            @RequestParam Integer trimestre,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.getMoyennes(classeId, trimestre, anneeScolaire)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Barèmes ---
    @GetMapping("/baremes")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<BaremeDTO>>> getAllBaremes() {
        return ResponseEntity.ok(ApiResponse.ok(noteService.getAllBaremes()));
    }

    @PostMapping("/baremes")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<BaremeDTO>> createBareme(@Valid @RequestBody BaremeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(noteService.createBareme(dto)));
    }

    @PutMapping("/baremes/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<BaremeDTO>> updateBareme(@PathVariable UUID id, @Valid @RequestBody BaremeDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.updateBareme(id, dto)));
    }

    // --- Compétences ---
    @GetMapping("/competences")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<CompetenceDTO>>> getCompetences(@RequestParam(required = false) UUID moduleId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.getCompetences(moduleId)));
    }

    @PostMapping("/competences")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<CompetenceDTO>> createCompetence(@Valid @RequestBody CompetenceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(noteService.createCompetence(dto)));
    }

    @DeleteMapping("/competences/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<Void> deleteCompetence(@PathVariable UUID id) {
        noteService.deleteCompetence(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/evaluations-competences")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<EvaluationCompetenceDTO>>> getEvaluations(
            @RequestParam UUID examenId, @RequestParam(required = false) UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.getEvaluationsCompetences(examenId, eleveId)));
    }

    @PostMapping("/evaluations-competences")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<EvaluationCompetenceDTO>> createEvaluation(@Valid @RequestBody EvaluationCompetenceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(noteService.createEvaluationCompetence(dto)));
    }
}
