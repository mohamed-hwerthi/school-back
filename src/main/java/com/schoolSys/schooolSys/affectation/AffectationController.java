package com.schoolSys.schooolSys.affectation;

import java.util.UUID;

import com.schoolSys.schooolSys.affectation.dto.AffectationBulkRequestDTO;
import com.schoolSys.schooolSys.affectation.dto.AffectationBulkResultDTO;
import com.schoolSys.schooolSys.affectation.dto.AffectationDTO;
import com.schoolSys.schooolSys.affectation.dto.AffectationRequestDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affectations")
@RequiredArgsConstructor
public class AffectationController {

    private final AffectationService affectationService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<List<AffectationDTO>>> search(
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID classeId,
            @RequestParam(required = false) UUID moduleId,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(
                affectationService.search(teacherId, classeId, moduleId, anneeScolaire)
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<AffectationDTO>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<AffectationDTO>> create(
            @Valid @RequestBody AffectationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(affectationService.create(request)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<AffectationBulkResultDTO>> bulkCreate(
            @Valid @RequestBody AffectationBulkRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(affectationService.bulkCreate(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<AffectationDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody AffectationRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.ok(affectationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        affectationService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
