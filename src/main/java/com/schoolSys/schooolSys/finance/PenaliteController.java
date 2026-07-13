package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.finance.dto.PenaliteRequestDTO;
import com.schoolSys.schooolSys.finance.dto.PenaliteResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalites")
@RequiredArgsConstructor
public class PenaliteController {

    private final PenaliteService penaliteService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<PenaliteResponseDTO>>> getAll(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(penaliteService.findByAnneeScolaire(anneeScolaire)));
    }

    @GetMapping("/eleve/{studentId}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<PenaliteResponseDTO>>> getByStudent(
            @PathVariable UUID studentId,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(penaliteService.findByStudentId(studentId, anneeScolaire)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<PenaliteResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(penaliteService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<PenaliteResponseDTO>> create(
            @Valid @RequestBody PenaliteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(penaliteService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<PenaliteResponseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody PenaliteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(penaliteService.update(id, dto)));
    }

    @PatchMapping("/{id}/toggle-payee")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<PenaliteResponseDTO>> togglePayee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(penaliteService.togglePayee(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        penaliteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
