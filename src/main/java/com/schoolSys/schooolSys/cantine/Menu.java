package com.schoolSys.schooolSys.cantine;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "menus")
@SQLDelete(sql = "UPDATE menus SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "date_menu", nullable = false)
    private LocalDate dateMenu;

    @Column(name = "jour_semaine", nullable = false, length = 10)
    private String jourSemaine;

    @Column(length = 300)
    private String entree;

    @Column(name = "plat_principal", nullable = false, length = 300)
    private String platPrincipal;

    @Column(length = 300)
    private String accompagnement;

    @Column(length = 300)
    private String dessert;

    @Column(name = "allergenes", columnDefinition = "TEXT[]")
    private String[] allergenes;

    @Column(name = "type_regime", length = 20)
    @Builder.Default
    private String typeRegime = "STANDARD";

    @Column
    private Integer semaine;

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
