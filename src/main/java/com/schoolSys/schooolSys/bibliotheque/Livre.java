package com.schoolSys.schooolSys.bibliotheque;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "livres")
@SQLDelete(sql = "UPDATE livres SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livre {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String titre;

    @Column(length = 200)
    private String auteur;

    @Column(length = 20)
    private String isbn;

    @Column(length = 100)
    private String categorie;

    @Column(length = 200)
    private String editeur;

    @Column(name = "annee_publication")
    private Integer anneePublication;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "nombre_exemplaires")
    @Builder.Default
    private Integer nombreExemplaires = 1;

    @Column(name = "exemplaires_disponibles")
    @Builder.Default
    private Integer exemplairesDisponibles = 1;

    @Column(length = 100)
    private String emplacement;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

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
