package com.schoolSys.schooolSys.note;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity @Table(name = "evaluations_competences")
@SQLDelete(sql = "UPDATE evaluations_competences SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EvaluationCompetence {
    @Id @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Column(name = "competence_id", nullable = false)
    private UUID competenceId;

    @Column(name = "examen_id", nullable = false)
    private UUID examenId;

    @Column(nullable = false, length = 20)
    private String niveau; // NON_ATTEINT, EN_COURS, ATTEINT, DEPASSE

    private String commentaire;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
