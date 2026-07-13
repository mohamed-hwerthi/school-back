package com.schoolSys.schooolSys.settings;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolSettings {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "school_name", nullable = false)
    @Builder.Default
    private String schoolName = "École";

    @Column(name = "school_name_ar")
    private String schoolNameAr;

    @Column(name = "annee_scolaire", nullable = false)
    @Builder.Default
    private String anneeScolaire = "2025 / 2026";

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(length = 50)
    private String telephone;

    @Column(name = "directeur_name")
    private String directeurName;

    @Column(name = "directeur_name_ar")
    private String directeurNameAr;

    /** Délégation régionale de l'éducation (ex : "Béja"). */
    @Column(name = "delegation_regionale", length = 120)
    private String delegationRegionale;

    @Column(name = "delegation_regionale_ar", length = 120)
    private String delegationRegionaleAr;

    @Column(columnDefinition = "TEXT")
    private String logo;

    @Column
    private String ville;

    @Column(name = "ville_ar")
    private String villeAr;

    @Column
    private String email;

    @Column(name = "site_web")
    private String siteWeb;

    @Column(name = "annee_creation", length = 10)
    private String anneeCreation;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
