package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.transport.dto.AffectationTransportDTO;
import com.schoolSys.schooolSys.transport.dto.CreateAffectationRequest;
import com.schoolSys.schooolSys.transport.dto.TransportStatsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affectations-transport")
@RequiredArgsConstructor
public class AffectationTransportController {

    private final AffectationTransportService affectationService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<List<AffectationTransportDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.getAll()));
    }

    @GetMapping("/circuit/{circuitId}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<List<AffectationTransportDTO>>> getByCircuit(@PathVariable UUID circuitId) {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.getByCircuit(circuitId)));
    }

    @GetMapping("/eleve/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<List<AffectationTransportDTO>>> getByEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.getByEleve(eleveId)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<TransportStatsDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.getStats()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<AffectationTransportDTO>> affecter(@Valid @RequestBody CreateAffectationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(affectationService.affecter(request)));
    }

    @PutMapping("/{id}/desaffecter")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<AffectationTransportDTO>> desaffecter(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.desaffecter(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        affectationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
