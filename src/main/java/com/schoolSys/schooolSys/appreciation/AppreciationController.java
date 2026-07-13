package com.schoolSys.schooolSys.appreciation;

import java.util.UUID;

import com.schoolSys.schooolSys.appreciation.dto.*;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appreciations")
@RequiredArgsConstructor
public class AppreciationController {

    private final AppreciationService appreciationService;

    // ── Recommandations ─────────────────────────────────────

    @GetMapping("/recommandations")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<RecommandationDTO>>> getRecommandations(
            @RequestParam List<UUID> studentIds,
            @RequestParam Integer trimestre) {
        return ResponseEntity.ok(ApiResponse.ok(
                appreciationService.getRecommandations(studentIds, trimestre)));
    }

    @PostMapping("/recommandations/bulk")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<List<RecommandationDTO>>> upsertRecommandations(
            @Valid @RequestBody BulkRecommandationRequestDTO body) {
        return ResponseEntity.ok(ApiResponse.ok(
                appreciationService.upsertRecommandations(body.getItems())));
    }

    // ── Observations ────────────────────────────────────────

    @GetMapping("/observations")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<ObservationDTO>>> getObservations(
            @RequestParam List<UUID> studentIds,
            @RequestParam Integer trimestre) {
        return ResponseEntity.ok(ApiResponse.ok(
                appreciationService.getObservations(studentIds, trimestre)));
    }

    @PostMapping("/observations/bulk")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<List<ObservationDTO>>> upsertObservations(
            @Valid @RequestBody BulkObservationRequestDTO body) {
        return ResponseEntity.ok(ApiResponse.ok(
                appreciationService.upsertObservations(body.getItems())));
    }
}
