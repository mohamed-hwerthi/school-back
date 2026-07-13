package com.schoolSys.schooolSys.admin;

import com.schoolSys.schooolSys.admin.dto.AdminHomeDTO;
import com.schoolSys.schooolSys.admin.dto.TeacherPerformanceDTO;
import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints alimentant les cartes du home admin (KPI + alertes opérationnelles).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminHomeController {

    private final AdminHomeService adminHomeService;
    private final AnneeScolaireProvider anneeScolaireProvider;

    /** MOB-FUNC-025 — 6 KPI agrégés pour l'accueil admin (effectif, CA, impayés…). */
    @GetMapping("/dashboard-kpis")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<AdminHomeDTO.DashboardKpis>> getDashboardKpis(
            @RequestParam(required = false) String anneeScolaire) {
        String resolved = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return ResponseEntity.ok(ApiResponse.ok(adminHomeService.getDashboardKpis(resolved)));
    }

    /** MOB-FUNC-028 — alertes opérationnelles (impayés en retard, absentees, classes sans titulaire). */
    @GetMapping("/operational-alerts")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<AdminHomeDTO.OperationalAlerts>> getOperationalAlerts(
            @RequestParam(required = false) String anneeScolaire) {
        String resolved = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return ResponseEntity.ok(ApiResponse.ok(adminHomeService.getOperationalAlerts(resolved)));
    }

    /** MOB-FUNC-033 — tableau de performance enseignants. */
    @GetMapping("/teachers-performance")
    @PreAuthorize("hasAuthority('VIEW_REPORTS')")
    public ResponseEntity<ApiResponse<List<TeacherPerformanceDTO>>> getTeachersPerformance(
            @RequestParam(required = false) String anneeScolaire,
            @RequestParam(defaultValue = "1") int trimestre) {
        String resolved = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return ResponseEntity.ok(ApiResponse.ok(
                adminHomeService.getTeachersPerformance(resolved, trimestre)));
    }
}
