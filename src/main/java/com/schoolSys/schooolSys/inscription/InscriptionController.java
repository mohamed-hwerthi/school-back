package com.schoolSys.schooolSys.inscription;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.inscription.dto.*;
import com.schoolSys.schooolSys.student.dto.StudentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    /**
     * GET /api/inscriptions — List inscriptions with filters.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_INSCRIPTIONS')")
    public ResponseEntity<ApiResponse<PagedResponse<InscriptionDTO>>> getAll(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String anneeScolaire,
            @RequestParam(required = false) UUID niveauId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<InscriptionDTO> result = inscriptionService.findAll(statut, anneeScolaire, niveauId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/inscriptions/{id} — Get a single inscription.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_INSCRIPTIONS')")
    public ResponseEntity<ApiResponse<InscriptionDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(inscriptionService.findById(id)));
    }

    /**
     * PUT /api/inscriptions/{id}/statut — Update inscription status.
     */
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAuthority('MANAGE_INSCRIPTIONS')")
    public ResponseEntity<ApiResponse<InscriptionDTO>> updateStatut(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatutRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(inscriptionService.updateStatut(id, request)));
    }

    /**
     * GET /api/inscriptions/stats — Get inscription statistics.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('MANAGE_INSCRIPTIONS')")
    public ResponseEntity<ApiResponse<InscriptionStatsDTO>> getStats(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(inscriptionService.getStats(anneeScolaire)));
    }

    /**
     * GET /api/inscriptions/liste-attente/{niveauId} — Get waiting list for a niveau.
     */
    @GetMapping("/liste-attente/{niveauId}")
    @PreAuthorize("hasAuthority('MANAGE_INSCRIPTIONS')")
    public ResponseEntity<ApiResponse<List<InscriptionDTO>>> getListeAttente(
            @PathVariable UUID niveauId,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(inscriptionService.getListeAttente(niveauId, anneeScolaire)));
    }

    /**
     * POST /api/inscriptions/{id}/convert-to-student — Convert an accepted inscription into a student.
     */
    @PostMapping("/{id}/convert-to-student")
    @PreAuthorize("hasAuthority('MANAGE_INSCRIPTIONS')")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> convertToStudent(
            @PathVariable UUID id,
            @RequestParam(required = false) String classe,
            @RequestParam(required = false) String sexe) {
        return ResponseEntity.ok(ApiResponse.ok(inscriptionService.convertToStudent(id, classe, sexe)));
    }
}
