package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.rh.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rh/formations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_RH')")
public class FormationController {

    private final FormationService formationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FormationDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(formationService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FormationDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(formationService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FormationDTO>> create(
            @Valid @RequestBody CreateFormationRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(formationService.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FormationDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody CreateFormationRequest dto) {
        return ResponseEntity.ok(ApiResponse.ok(formationService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        formationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/participants")
    public ResponseEntity<ApiResponse<FormationParticipantDTO>> addParticipant(
            @PathVariable UUID id, @Valid @RequestBody AddParticipantRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(formationService.addParticipant(id, dto)));
    }

    @DeleteMapping("/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID participantId) {
        formationService.removeParticipant(participantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<RhStatsDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(formationService.getStats()));
    }
}
