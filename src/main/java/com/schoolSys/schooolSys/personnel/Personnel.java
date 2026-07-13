package com.schoolSys.schooolSys.personnel;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

/**
 * Non-teaching staff member (secrétaire, comptable, gardien, femme de ménage…).
 * <p>
 * Teaching staff live in {@link com.schoolSys.schooolSys.teacher.Teacher};
 * this entity is a pure HR record (no login account is provisioned) so the
 * school can manage every other employee category.
 * </p>
 */
@Entity
@Table(name = "personnel")
@SQLDelete(sql = "UPDATE personnel SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Personnel {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    /** Free-form job title (Secrétaire, Comptable, Gardien… or any custom value). */
    @Column(nullable = false)
    private String fonction;

    private String email;

    private String sexe;

    private String telephone;

    private LocalDate dateNaissance;

    @Column(columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private LocalDate dateEmbauche;

    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'Actif'")
    private String statut;

    @PrePersist
    private void prePersist() {
        if (dateEmbauche == null) dateEmbauche = LocalDate.now();
        if (statut == null) statut = "Actif";
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
