package com.schoolSys.schooolSys.conseilclasse;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.conseilclasse.dto.ConseilClasseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Conseil de classe — annual averages and proposed end-of-year decisions
 * (ANN-010/011). Restricted to the directeur via {@code MANAGE_ANNEE_SCOLAIRE}.
 * Validated decisions are persisted through {@code POST /api/passages/bulk}.
 */
@RestController
@RequestMapping("/api/conseil-classe")
@RequiredArgsConstructor
public class ConseilClasseController {

    private final ConseilClasseService conseilClasseService;

    @GetMapping("/{classeId}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<ConseilClasseDTO>> getConseilClasse(@PathVariable UUID classeId) {
        return ResponseEntity.ok(ApiResponse.ok(conseilClasseService.getConseilClasse(classeId)));
    }
}
