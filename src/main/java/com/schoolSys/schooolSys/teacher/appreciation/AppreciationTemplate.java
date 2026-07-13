package com.schoolSys.schooolSys.teacher.appreciation;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MOB-FUNC-023 — modèle d'appréciation réutilisable pour la saisie de bulletins.
 * Scope : un enseignant ne voit que ses propres modèles.
 */
@Entity
@Table(name = "appreciation_templates")
@SQLDelete(sql = "UPDATE appreciation_templates SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppreciationTemplate {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "enseignant_id", nullable = false)
    private UUID enseignantId;

    @Column(nullable = false, length = 60)
    private String libelle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    /** "POSITIF" | "NEUTRE" | "NEGATIF" — contrainte en BD. */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String tag = "NEUTRE";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
