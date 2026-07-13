package com.schoolSys.schooolSys.rapport;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "rapports")
@SQLDelete(sql = "UPDATE rapports SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rapport {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(length = 100)
    private String periode;

    @Column(name = "date_generation", nullable = false)
    @Builder.Default
    private LocalDateTime dateGeneration = LocalDateTime.now();

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String statut = "Brouillon";

    @Column(nullable = false, length = 255)
    private String auteur;

    @Column(length = 255)
    private String destinataire;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String fichier;

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
}
