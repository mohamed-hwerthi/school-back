package com.schoolSys.schooolSys.discipline;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import com.schoolSys.schooolSys.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "sanctions")
@SQLDelete(sql = "UPDATE sanctions SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sanction extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Incident incident;

    @Column(nullable = false, length = 50)
    private String type; // AVERTISSEMENT, BLAME, EXCLUSION_TEMPORAIRE, EXCLUSION_DEFINITIVE, TRAVAIL_SUPPLEMENTAIRE

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "decide_par_id")
    private UUID decideParId;

    @Column(name = "notifie_parents")
    @Builder.Default
    private Boolean notifieParents = false;

    @Column(name = "niveau")
    @Builder.Default
    private Integer niveau = 1; // 1=AVERTISSEMENT, 2=BLAME, 3=EXCLUSION_TEMPORAIRE, 4=EXCLUSION_DEFINITIVE

    @Column(name = "statut", length = 30)
    @Builder.Default
    private String statut = "ACTIVE"; // ACTIVE, LEVEE, EXPIREE

    @Column(name = "approuve_par")
    private UUID approuvePar;

    @Column(name = "commentaire_approbation", columnDefinition = "TEXT")
    private String commentaireApprobation;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
