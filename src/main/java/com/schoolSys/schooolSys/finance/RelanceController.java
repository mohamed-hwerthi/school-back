package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.finance.dto.RelanceRequestDTO;
import com.schoolSys.schooolSys.finance.dto.RelanceResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relances")
@RequiredArgsConstructor
public class RelanceController {

    private final RelanceService relanceService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<RelanceResponseDTO>>> getAll(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(relanceService.findByAnneeScolaire(anneeScolaire)));
    }

    @GetMapping("/eleve/{studentId}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<RelanceResponseDTO>>> getByStudent(
            @PathVariable UUID studentId,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(relanceService.findByStudent(studentId, anneeScolaire)));
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<RelanceResponseDTO>>> getPending(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(relanceService.findPending(anneeScolaire)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<RelanceService.RelanceStatsDTO>> getStats(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(relanceService.getStats(anneeScolaire)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<RelanceResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(relanceService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<RelanceResponseDTO>> create(
            @Valid @RequestBody RelanceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(relanceService.create(dto)));
    }

    @PostMapping("/generer")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<List<RelanceResponseDTO>>> generate(
            @RequestParam(required = false) String anneeScolaire,
            @RequestParam(defaultValue = "EMAIL") Relance.TypeRelance type) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(relanceService.generateRelances(anneeScolaire, type)));
    }

    @PatchMapping("/{id}/envoyee")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<RelanceResponseDTO>> markEnvoyee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(relanceService.markAsEnvoyee(id)));
    }

    @PatchMapping("/{id}/echouee")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<RelanceResponseDTO>> markEchouee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(relanceService.markAsEchouee(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        relanceService.delete(id);
    }
}
