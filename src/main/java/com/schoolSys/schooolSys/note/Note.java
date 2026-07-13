package com.schoolSys.schooolSys.note;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import com.schoolSys.schooolSys.common.audit.AuditableEntity;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.student.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "notes", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "examen_id", "trimestre"}))
@SQLDelete(sql = "UPDATE notes SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Examen examen;

    @Column(name = "annee_scolaire", nullable = false)
    private String anneeScolaire;

    @Column(nullable = false)
    private Integer trimestre;

    private Double valeur;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String statut = "PRESENT";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
