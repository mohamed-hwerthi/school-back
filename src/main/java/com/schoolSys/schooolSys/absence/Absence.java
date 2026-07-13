package com.schoolSys.schooolSys.absence;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "absences")
@SQLDelete(sql = "UPDATE absences SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "annee_scolaire", nullable = false)
    private String anneeScolaire;

    @Column(nullable = false, length = 20)
    private String type; // ABSENCE or RETARD

    @Column(length = 50)
    private String seance;

    @Column(name = "heure_arrivee")
    private LocalTime heureArrivee;

    @Column(nullable = false)
    @Builder.Default
    private Boolean justifie = false;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(name = "enseignant_id")
    private UUID enseignantId;

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
