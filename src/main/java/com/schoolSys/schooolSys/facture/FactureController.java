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

import java.util.List;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_FACTURES')")
public class FactureController {

    private final FactureService factureService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FactureResponseDTO>>> getAllFactures() {
        return ResponseEntity.ok(ApiResponse.ok(factureService.getAllFactures()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FactureResponseDTO>> getFactureById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(factureService.getFactureById(id)));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<FactureResponseDTO>>> getFacturesByStudent(
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.ok(factureService.getFacturesByStudent(studentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FactureResponseDTO>> createFacture(
            @Valid @RequestBody FactureRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(factureService.createFacture(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FactureResponseDTO>> updateFacture(
            @PathVariable UUID id, @Valid @RequestBody FactureRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(factureService.updateFacture(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable UUID id) {
        factureService.deleteFacture(id);
        return ResponseEntity.noContent().build();
    }
}
