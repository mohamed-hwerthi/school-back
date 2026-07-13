package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "reponses_eleve")
@SQLDelete(sql = "UPDATE reponses_eleve SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReponseEleve {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tentative_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Tentative tentative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choix_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChoixReponse choix;

    @Column(name = "reponse_texte", columnDefinition = "TEXT")
    private String reponseTexte;

    @Column
    private Boolean correct;

    @Column(name = "points_obtenus", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pointsObtenus = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
