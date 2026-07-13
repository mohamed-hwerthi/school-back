package com.schoolSys.schooolSys.notification;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "annonces")
@SQLDelete(sql = "UPDATE annonces SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Annonce {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String type = "GENERAL";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String destinataires = "TOUS";

    @Column(name = "classe_id")
    private UUID classeId;

    @Column(name = "auteur_id")
    private UUID auteurId;

    @Column(name = "auteur_name", length = 100)
    private String auteurName;

    @Column(name = "date_publication")
    @Builder.Default
    private LocalDateTime datePublication = LocalDateTime.now();

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
