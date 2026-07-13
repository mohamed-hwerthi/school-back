package com.schoolSys.schooolSys.facture;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.facture.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/echeanciers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_FACTURES')")
public class EcheancierController {

    private final FactureService factureService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EcheancierResponseDTO>>> getAllEcheanciers() {
        return ResponseEntity.ok(ApiResponse.ok(factureService.getAllEcheanciers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EcheancierResponseDTO>> getEcheancierById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(factureService.getEcheancierById(id)));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<EcheancierResponseDTO>>> getEcheanciersByStudent(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.ok(factureService.getEcheanciersByStudent(studentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EcheancierResponseDTO>> createEcheancier(
            @Valid @RequestBody EcheancierRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(factureService.createEcheancier(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EcheancierResponseDTO>> updateEcheancier(
            @PathVariable UUID id, @Valid @RequestBody EcheancierRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(factureService.updateEcheancier(id, dto)));
    }

    @PutMapping("/echeances/{echeanceId}/payer")
    public ResponseEntity<ApiResponse<EcheanceDTO>> payerEcheance(
            @PathVariable UUID echeanceId, @RequestParam BigDecimal montant) {
        return ResponseEntity.ok(ApiResponse.ok(factureService.payerEcheance(echeanceId, montant)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEcheancier(@PathVariable UUID id) {
        factureService.deleteEcheancier(id);
        return ResponseEntity.noContent().build();
    }
}
