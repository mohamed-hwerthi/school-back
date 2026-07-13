package com.schoolSys.schooolSys.scolarite;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Historised enrolment of a student for one school year (ANN-004):
 * which niveau/classe they were in, and how they got there (statut).
 */
@Entity
@Table(name = "scolarites", uniqueConstraints =
        @UniqueConstraint(columnNames = {"student_id", "annee_scolaire"}))
@SQLDelete(sql = "UPDATE scolarites SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scolarite {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "annee_scolaire", nullable = false, length = 20)
    private String anneeScolaire;

    @Column(length = 100)
    private String niveau;

    @Column(length = 50)
    private String classe;

    /** INSCRIT (first enrolment) or REINSCRIT (rolled over from the previous year). */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String statut = "INSCRIT";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
