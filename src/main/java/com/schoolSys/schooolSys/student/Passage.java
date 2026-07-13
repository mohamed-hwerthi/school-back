package com.schoolSys.schooolSys.student;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaire;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "passages")
@SQLDelete(sql = "UPDATE passages SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passage {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "ancien_niveau", length = 100)
    private String ancienNiveau;

    @Column(name = "nouveau_niveau", length = 100)
    private String nouveauNiveau;

    @Column(name = "ancienne_classe", length = 50)
    private String ancienneClasse;

    @Column(name = "nouvelle_classe", length = 50)
    private String nouvelleClasse;

    @Column(nullable = false, length = 20)
    private String decision;

    @Column(name = "annee_scolaire", nullable = false, length = 20)
    private String anneeScolaire;

    /** ANN-003: FK to the academic year; {@code anneeScolaire} above is kept as a denormalised label. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annee_scolaire_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private AnneeScolaire anneeScolaireRef;

    @Column(columnDefinition = "TEXT")
    private String motif;

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
