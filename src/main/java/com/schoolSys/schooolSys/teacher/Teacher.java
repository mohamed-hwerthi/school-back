package com.schoolSys.schooolSys.teacher;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
@SQLDelete(sql = "UPDATE teachers SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String email;

    private String specialization;

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
