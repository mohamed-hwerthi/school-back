package com.schoolSys.schooolSys.bilanannuel;

import com.schoolSys.schooolSys.bilanannuel.dto.BilanAnnuelDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Bilan annuel — end-of-year decision statistics (ANN-020/021).
 * Restricted to the directeur via {@code MANAGE_ANNEE_SCOLAIRE}.
 */
@RestController
@RequestMapping("/api/bilan-annuel")
@RequiredArgsConstructor
public class BilanAnnuelController {

    private final BilanAnnuelService bilanAnnuelService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<BilanAnnuelDTO>> getBilan(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(bilanAnnuelService.getBilan(anneeScolaire)));
    }

    /** ANN-023 — inter-year comparison: one bilan per recorded school year. */
    @GetMapping("/comparatif")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<List<BilanAnnuelDTO>>> getComparatif() {
        return ResponseEntity.ok(ApiResponse.ok(bilanAnnuelService.getComparatif()));
    }
}
