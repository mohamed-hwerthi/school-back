package com.schoolSys.schooolSys.teacher;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "teacher_evaluations")
@SQLDelete(sql = "UPDATE teacher_evaluations SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherEvaluation {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "evaluator_id")
    private UUID evaluatorId;

    @Column(name = "evaluator_name", length = 100)
    private String evaluatorName;

    @Column(name = "annee_scolaire", nullable = false, length = 20)
    private String anneeScolaire;

    @Column(nullable = false)
    private Integer trimestre;

    private Integer ponctualite;

    private Integer pedagogie;

    private Integer discipline;

    private Integer communication;

    private Integer implication;

    @Column(name = "note_globale", precision = 3, scale = 1)
    private BigDecimal noteGlobale;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        computeNoteGlobale();
    }

    @PreUpdate
    private void preUpdate() {
        computeNoteGlobale();
    }

    private void computeNoteGlobale() {
        int count = 0;
        int sum = 0;
        if (ponctualite != null) { sum += ponctualite; count++; }
        if (pedagogie != null) { sum += pedagogie; count++; }
        if (discipline != null) { sum += discipline; count++; }
        if (communication != null) { sum += communication; count++; }
        if (implication != null) { sum += implication; count++; }
        if (count > 0) {
            noteGlobale = BigDecimal.valueOf((double) sum / count).setScale(1, java.math.RoundingMode.HALF_UP);
        }
    }

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
