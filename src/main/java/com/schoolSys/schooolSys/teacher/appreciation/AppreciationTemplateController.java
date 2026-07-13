package com.schoolSys.schooolSys.teacher.appreciation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.teacher.Teacher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MOB-FUNC-023 — CRUD des modèles d'appréciation de l'enseignant courant.
 */
@RestController
@RequestMapping("/api/teacher/appreciation-templates")
@RequiredArgsConstructor
public class AppreciationTemplateController {

    private final AppreciationTemplateRepository repository;
    private final CurrentUserContext currentUser;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<AppreciationTemplateDTO>>> list() {
        UUID teacherId = currentTeacherIdOrThrow();
        List<AppreciationTemplateDTO> items = repository.findByEnseignantIdOrderByLibelleAsc(teacherId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('READ_NOTES')")
    @Transactional
    public ResponseEntity<ApiResponse<AppreciationTemplateDTO>> create(
            @Valid @RequestBody UpsertRequest body) {
        UUID teacherId = currentTeacherIdOrThrow();
        AppreciationTemplate saved = repository.save(AppreciationTemplate.builder()
                .enseignantId(teacherId)
                .libelle(body.getLibelle().trim())
                .contenu(body.getContenu().trim())
                .tag(body.getTag())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build());
        return ResponseEntity.ok(ApiResponse.ok(toDto(saved)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    @Transactional
    public ResponseEntity<ApiResponse<AppreciationTemplateDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpsertRequest body) {
        UUID teacherId = currentTeacherIdOrThrow();
        AppreciationTemplate t = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        if (!teacherId.equals(t.getEnseignantId())) {
            throw new ResourceNotFoundException("Template not found");
        }
        t.setLibelle(body.getLibelle().trim());
        t.setContenu(body.getContenu().trim());
        t.setTag(body.getTag());
        AppreciationTemplate saved = repository.save(t);
        return ResponseEntity.ok(ApiResponse.ok(toDto(saved)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        UUID teacherId = currentTeacherIdOrThrow();
        AppreciationTemplate t = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        if (!teacherId.equals(t.getEnseignantId())) {
            throw new ResourceNotFoundException("Template not found");
        }
        repository.delete(t); // soft-delete via @SQLDelete
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private UUID currentTeacherIdOrThrow() {
        return currentUser.getCurrentTeacher()
                .map(Teacher::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant courant introuvable"));
    }

    private AppreciationTemplateDTO toDto(AppreciationTemplate t) {
        return AppreciationTemplateDTO.builder()
                .id(t.getId())
                .libelle(t.getLibelle())
                .contenu(t.getContenu())
                .tag(t.getTag())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    // ── DTOs locaux ─────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppreciationTemplateDTO {
        private UUID id;
        private String libelle;
        private String contenu;
        private String tag;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpsertRequest {
        @NotBlank @Size(max = 60)
        private String libelle;

        @NotBlank
        private String contenu;

        @NotBlank
        @Pattern(regexp = "POSITIF|NEUTRE|NEGATIF", message = "tag must be POSITIF, NEUTRE or NEGATIF")
        private String tag;
    }
}
