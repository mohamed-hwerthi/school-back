package com.schoolSys.schooolSys.devoir;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "soumissions")
@SQLDelete(sql = "UPDATE soumissions SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Soumission {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devoir_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Devoir devoir;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "fichier_url", length = 500)
    private String fichierUrl;

    @Column(name = "date_soumission")
    @Builder.Default
    private LocalDateTime dateSoumission = LocalDateTime.now();

    @Column(precision = 5, scale = 2)
    private BigDecimal note;

    @Column(name = "commentaire_correction", columnDefinition = "TEXT")
    private String commentaireCorrection;

    @Column
    @Builder.Default
    private Boolean corrige = false;

    @Column(name = "en_retard")
    @Builder.Default
    private Boolean enRetard = false;

    @Column(name = "annee_scolaire", nullable = false)
    private String anneeScolaire;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
