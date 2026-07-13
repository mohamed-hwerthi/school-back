package com.schoolSys.schooolSys.bulletin;

import java.util.UUID;

import com.schoolSys.schooolSys.bulletin.dto.*;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequestMapping("/api/bulletins")
@RequiredArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;
    private final BulletinZipService bulletinZipService;

    @GetMapping
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<List<BulletinDTO>>> getBulletins(
            @RequestParam UUID classeId,
            @RequestParam Integer trimestre,
            @RequestParam(defaultValue = "etatique") String version) {
        return ResponseEntity.ok(ApiResponse.ok(
                bulletinService.getBulletins(classeId, trimestre, version)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<BulletinDTO>> getBulletin(
            @RequestParam UUID classeId,
            @PathVariable UUID studentId,
            @RequestParam Integer trimestre,
            @RequestParam(defaultValue = "etatique") String version) {
        return ResponseEntity.ok(ApiResponse.ok(
                bulletinService.getBulletin(classeId, studentId, trimestre, version)));
    }

    /**
     * Génère un ZIP contenant un PDF par bulletin de la classe. Le rendu PDF
     * est server-side (OpenHTMLToPDF + Amiri) — beaucoup plus rapide que
     * l'ancien rendu html2canvas côté navigateur pour 30+ bulletins.
     */
    @GetMapping("/zip")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<StreamingResponseBody> downloadBulletinsZip(
            @RequestParam UUID classeId,
            @RequestParam Integer trimestre,
            @RequestParam(defaultValue = "etatique") String version) {
        String filename = "Bulletins_T" + trimestre + "_" + version + ".zip";

        // TenantContext est un ThreadLocal — il faut le capturer ici puis le
        // réinjecter dans le thread async du StreamingResponseBody.
        String tenant = TenantContext.getCurrentTenant();
        StreamingResponseBody body = out -> {
            TenantContext.setCurrentTenant(tenant);
            try {
                bulletinZipService.writeZip(classeId, trimestre, version, out);
            } finally {
                TenantContext.clear();
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    // --- ANN-040: Bulletin annuel ---

    @GetMapping("/annuel")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<List<BulletinAnnuelDTO>>> getBulletinsAnnuels(
            @RequestParam UUID classeId,
            @RequestParam(defaultValue = "etatique") String version) {
        return ResponseEntity.ok(ApiResponse.ok(
                bulletinService.getBulletinsAnnuels(classeId, version)));
    }

    // --- ANN-025: Taux de réussite par matière ---

    @GetMapping("/stats-matieres")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<List<MatiereStatDTO>>> getStatsMatieres(
            @RequestParam UUID classeId,
            @RequestParam(defaultValue = "etatique") String version) {
        return ResponseEntity.ok(ApiResponse.ok(
                bulletinService.getStatsMatieresAnnuelles(classeId, version)));
    }

    // --- BUL-003: Templates ---

    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<List<BulletinTemplateDTO>>> getTemplates() {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.getAllTemplates()));
    }

    @GetMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<BulletinTemplateDTO>> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.getTemplate(id)));
    }

    @GetMapping("/templates/active")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<BulletinTemplateDTO>> getActiveTemplate() {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.getActiveTemplate()));
    }

    @PostMapping("/templates")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<BulletinTemplateDTO>> createTemplate(
            @Valid @RequestBody BulletinTemplateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(bulletinService.createTemplate(dto)));
    }

    @PutMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<BulletinTemplateDTO>> updateTemplate(
            @PathVariable UUID id, @Valid @RequestBody BulletinTemplateDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.updateTemplate(id, dto)));
    }

    @PutMapping("/templates/{id}/activate")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<BulletinTemplateDTO>> activateTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.activateTemplate(id)));
    }

    @DeleteMapping("/templates/{id}")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        bulletinService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    // --- BUL-004: Mass generate ---

    @GetMapping("/classe/{classeId}/trimestre/{trimestre}/mass-generate")
    @PreAuthorize("hasAuthority('GENERATE_BULLETINS')")
    public ResponseEntity<ApiResponse<List<BulletinDTO>>> massGenerate(
            @PathVariable UUID classeId, @PathVariable Integer trimestre) {
        return ResponseEntity.ok(ApiResponse.ok(
                bulletinService.generateBulletinsForClasse(classeId, trimestre)));
    }

    // --- BUL-005: Stats reussite ---

    @GetMapping("/stats/classe/{classeId}/trimestre/{trimestre}")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<StatsReussiteDTO>> getStatsReussite(
            @PathVariable UUID classeId, @PathVariable Integer trimestre) {
        return ResponseEntity.ok(ApiResponse.ok(
                bulletinService.getStatsReussite(classeId, trimestre)));
    }

    // --- BUL-006: Attestation ---

    @GetMapping("/attestation/{eleveId}")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<AttestationDTO>> getAttestation(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.getAttestation(eleveId)));
    }

    // --- BUL-007: Comparatif ---

    @GetMapping("/comparatif")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<ComparatifDTO>> getComparatifByNiveau(
            @RequestParam UUID niveauId) {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.getComparatifByNiveau(niveauId)));
    }

    @GetMapping("/comparatif/evolution")
    @PreAuthorize("hasAuthority('READ_BULLETINS')")
    public ResponseEntity<ApiResponse<ComparatifDTO>> getComparatifEvolution(
            @RequestParam UUID classeId) {
        return ResponseEntity.ok(ApiResponse.ok(bulletinService.getComparatifEvolution(classeId)));
    }
}
