package com.schoolSys.schooolSys.scolarite;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.scolarite.dto.AttestationReussiteDTO;
import com.schoolSys.schooolSys.scolarite.dto.ReinscriptionResultDTO;
import com.schoolSys.schooolSys.scolarite.dto.ScolariteDTO;
import com.schoolSys.schooolSys.scolarite.dto.SuiviReinscriptionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Historised schooling and re-enrolment (ANN-004/050/051/042).
 */
@RestController
@RequestMapping("/api/scolarites")
@RequiredArgsConstructor
public class ScolariteController {

    private final ScolariteService scolariteService;

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<ApiResponse<List<ScolariteDTO>>> getHistorique(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.ok(scolariteService.getHistorique(studentId)));
    }

    @PostMapping("/reinscription")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<ReinscriptionResultDTO>> reinscrire(
            @RequestParam String anneeSource,
            @RequestParam String anneeDestination) {
        return ResponseEntity.ok(ApiResponse.ok(
                scolariteService.reinscrire(anneeSource, anneeDestination)));
    }

    @GetMapping("/suivi")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<SuiviReinscriptionDTO>> getSuivi(
            @RequestParam String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(scolariteService.getSuivi(anneeScolaire)));
    }

    @GetMapping("/attestation-reussite")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<AttestationReussiteDTO>> getAttestationReussite(
            @RequestParam UUID studentId,
            @RequestParam String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(
                scolariteService.getAttestationReussite(studentId, anneeScolaire)));
    }
}
