package com.schoolSys.schooolSys.cloture;

import java.util.UUID;

import com.schoolSys.schooolSys.cloture.dto.ClotureRequestDTO;
import com.schoolSys.schooolSys.cloture.dto.ClotureResultDTO;
import com.schoolSys.schooolSys.cloture.dto.PreChecksResponseDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Year-end closure (ANN-030/031/033). Restricted to the directeur via
 * {@code MANAGE_ANNEE_SCOLAIRE}.
 */
@RestController
@RequestMapping("/api/cloture")
@RequiredArgsConstructor
public class ClotureController {

    private final ClotureService clotureService;

    @GetMapping("/{anneeId}/pre-checks")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<PreChecksResponseDTO>> getPreChecks(@PathVariable UUID anneeId) {
        return ResponseEntity.ok(ApiResponse.ok(clotureService.getPreChecks(anneeId)));
    }

    @PostMapping("/{anneeId}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<ClotureResultDTO>> cloturer(
            @PathVariable UUID anneeId,
            @RequestBody(required = false) ClotureRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(clotureService.cloturer(anneeId, request)));
    }
}
