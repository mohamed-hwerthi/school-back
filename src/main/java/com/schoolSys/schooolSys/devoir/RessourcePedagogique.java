package com.schoolSys.schooolSys.devoir;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "ressources_pedagogiques")
@SQLDelete(sql = "UPDATE ressources_pedagogiques SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RessourcePedagogique {

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

    @Column(length = 20)
    @Builder.Default
    private String type = "DOCUMENT";

    @Column(name = "fichier_url", length = 500)
    private String fichierUrl;

    @Column(name = "lien_externe", length = 500)
    private String lienExterne;

    @Column(name = "enseignant_id")
    private UUID enseignantId;

    @Column(name = "taille_fichier")
    private Long tailleFichier;

    @Column(name = "annee_scolaire", nullable = false)
    private String anneeScolaire;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
