package com.schoolSys.schooolSys.bulletin;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity @Table(name = "bulletin_templates")
@SQLDelete(sql = "UPDATE bulletin_templates SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BulletinTemplate {
    @Id @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "nom_ecole_fr")
    private String nomEcoleFr;

    @Column(name = "nom_ecole_ar")
    private String nomEcoleAr;

    @Column(length = 500)
    private String adresse;

    @Column(length = 50)
    private String telephone;

    @Column(length = 100)
    private String email;

    @Column(name = "header_color", length = 7)
    @Builder.Default
    private String headerColor = "#1e40af";

    @Column(name = "show_logo")
    @Builder.Default
    private Boolean showLogo = true;

    @Column(name = "show_photo_eleve")
    @Builder.Default
    private Boolean showPhotoEleve = false;

    @Column(name = "show_appreciation")
    @Builder.Default
    private Boolean showAppreciation = true;

    @Column(name = "show_rang")
    @Builder.Default
    private Boolean showRang = true;

    @Column(name = "footer_text", columnDefinition = "TEXT")
    private String footerText;

    @Builder.Default
    private Boolean actif = true;

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
