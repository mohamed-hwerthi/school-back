package com.schoolSys.schooolSys.transport;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "vehicules")
@SQLDelete(sql = "UPDATE vehicules SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicule {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String immatriculation;

    @Column(length = 100)
    private String marque;

    @Column(length = 100)
    private String modele;

    @Column(nullable = false)
    private Integer capacite;

    @Column(name = "chauffeur_nom", length = 200)
    private String chauffeurNom;

    @Column(name = "chauffeur_telephone", length = 20)
    private String chauffeurTelephone;

    @Column(name = "date_assurance")
    private LocalDate dateAssurance;

    @Column(name = "date_controle_technique")
    private LocalDate dateControleTechnique;

    @Column(length = 20)
    @Builder.Default
    private String statut = "ACTIF";

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
