package com.schoolSys.schooolSys.analytics;

import java.util.UUID;

import com.schoolSys.schooolSys.analytics.dto.*;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final KpiConfigService kpiConfigService;

    // ── Analytics endpoints (VIEW_REPORTS) ──

    @GetMapping("/suivi-eleve/{eleveId}")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<SuiviEleveDTO>> getSuiviEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getSuiviEleve(eleveId)));
    }

    @GetMapping("/eleves-a-risque")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<SuiviEleveDTO>>> getElevesARisque(
            @RequestParam(defaultValue = "70") Double seuil) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getElevesARisque(seuil)));
    }

    @GetMapping("/comparaison-classes")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<ComparaisonClassesDTO>> getComparaisonClasses() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getComparaisonClasses()));
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<KpiDTO>>> getKpis() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getKpis()));
    }

    @GetMapping("/cohortes")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<CohorteDTO>>> getCohortes(
            @RequestParam(required = false) UUID niveauId) {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getCohortes(niveauId)));
    }

    // ── KPI Config endpoints (MANAGE_SETTINGS) ──

    @GetMapping("/kpi-config")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<ApiResponse<List<KpiConfigResponseDTO>>> getAllKpiConfigs() {
        return ResponseEntity.ok(ApiResponse.ok(kpiConfigService.findAll()));
    }

    @GetMapping("/kpi-config/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<ApiResponse<KpiConfigResponseDTO>> getKpiConfig(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(kpiConfigService.findById(id)));
    }

    @PostMapping("/kpi-config")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<ApiResponse<KpiConfigResponseDTO>> createKpiConfig(@RequestBody KpiConfigRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(kpiConfigService.create(dto)));
    }

    @PutMapping("/kpi-config/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<ApiResponse<KpiConfigResponseDTO>> updateKpiConfig(
            @PathVariable UUID id, @RequestBody KpiConfigRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(kpiConfigService.update(id, dto)));
    }

    @DeleteMapping("/kpi-config/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<Void> deleteKpiConfig(@PathVariable UUID id) {
        kpiConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
