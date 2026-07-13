package com.schoolSys.schooolSys.devoir;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "devoirs")
@SQLDelete(sql = "UPDATE devoirs SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devoir {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "classe_id")
    private UUID classeId;

    @Column(name = "enseignant_id")
    private UUID enseignantId;

    @Column(name = "date_publication", nullable = false)
    @Builder.Default
    private LocalDate datePublication = LocalDate.now();

    @Column(name = "date_limite", nullable = false)
    private LocalDate dateLimite;

    @Column(length = 20)
    @Builder.Default
    private String type = "DEVOIR";

    @Column(name = "points_max")
    @Builder.Default
    private Integer pointsMax = 20;

    @Column(name = "fichier_url", length = 500)
    private String fichierUrl;

    @Column(length = 20)
    @Builder.Default
    private String statut = "PUBLIE";

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
