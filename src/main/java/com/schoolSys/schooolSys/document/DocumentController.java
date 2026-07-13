package com.schoolSys.schooolSys.document;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.document.dto.DocumentGenerationRequest;
import com.schoolSys.schooolSys.document.dto.DocumentHistoryDTO;
import com.schoolSys.schooolSys.document.dto.DocumentTemplateConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentGenerationService generationService;
    private final DocumentTemplateService templateService;

    @PostMapping("/certificat-scolarite/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<byte[]> generateCertificatScolarite(@PathVariable UUID eleveId) {
        byte[] content = generationService.generateCertificatScolarite(eleveId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificat_scolarite_" + eleveId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    @PostMapping("/carte-scolaire/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<byte[]> generateCarteScolaire(@PathVariable UUID eleveId) {
        byte[] content = generationService.generateCarteScolaire(eleveId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=carte_scolaire_" + eleveId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    @PostMapping("/attestation/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<byte[]> generateAttestation(
            @PathVariable UUID eleveId,
            @RequestParam(required = false) String anneeScolaire) {
        byte[] content = generationService.generateAttestationReussite(eleveId, anneeScolaire);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attestation_" + eleveId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    @PostMapping("/releve-notes/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<byte[]> generateReleveNotes(
            @PathVariable UUID eleveId,
            @RequestParam(required = false) Integer trimestre) {
        byte[] content = generationService.generateReleveNotes(eleveId, trimestre);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=releve_notes_" + eleveId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    @PostMapping("/recu-paiement/{paiementId}")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<byte[]> generateRecuPaiement(@PathVariable UUID paiementId) {
        byte[] content = generationService.generateRecuPaiement(paiementId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu_paiement_" + paiementId + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(content);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<byte[]> generateBulk(@RequestBody DocumentGenerationRequest request) {
        byte[] content = generationService.generateBulk(request.getEleveIds(), request.getType().name());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documents_bulk.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    @GetMapping("/historique")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<ApiResponse<List<DocumentHistoryDTO>>> getHistorique() {
        return ResponseEntity.ok(ApiResponse.ok(generationService.getHistory()));
    }

    @GetMapping("/template-config")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<ApiResponse<DocumentTemplateConfig>> getTemplateConfig() {
        return ResponseEntity.ok(ApiResponse.ok(templateService.getTemplateConfig()));
    }

    @PutMapping("/template-config")
    @PreAuthorize("hasAuthority('MANAGE_SETTINGS')")
    public ResponseEntity<ApiResponse<DocumentTemplateConfig>> updateTemplateConfig(
            @RequestBody DocumentTemplateConfig config) {
        return ResponseEntity.ok(ApiResponse.ok(templateService.updateTemplateConfig(config)));
    }
}
