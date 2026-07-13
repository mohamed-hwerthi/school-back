package com.schoolSys.schooolSys.inscription;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "inscriptions")
@SQLDelete(sql = "UPDATE inscriptions SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 200)
    private String lieuNaissance;

    @Column(length = 1)
    private String sexe;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "telephone_parent", length = 20)
    private String telephoneParent;

    @Column(name = "email_parent", length = 200)
    private String emailParent;

    @Column(name = "nom_parent", length = 200)
    private String nomParent;

    @Column(name = "prenom_parent", length = 200)
    private String prenomParent;

    @Column(name = "niveau_id")
    private UUID niveauId;

    @Column(name = "annee_scolaire", nullable = false, length = 20)
    private String anneeScolaire;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String statut = "SOUMISE";

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "numero_dossier", unique = true, length = 50)
    private String numeroDossier;

    @Column(name = "documents_paths", columnDefinition = "TEXT[]")
    private String[] documentsPaths;

    @Column(name = "montant_frais", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantFrais = BigDecimal.ZERO;

    @Column(name = "frais_paye")
    @Builder.Default
    private Boolean fraisPaye = false;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
