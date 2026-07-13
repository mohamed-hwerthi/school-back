package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "quiz")
@SQLDelete(sql = "UPDATE quiz SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "classe_id")
    private UUID classeId;

    @Column(name = "enseignant_id")
    private UUID enseignantId;

    @Column(name = "duree_minutes", nullable = false)
    @Builder.Default
    private Integer dureeMinutes = 60;

    @Column(name = "note_totale", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal noteTotale = new BigDecimal("20");

    @Column(name = "melanger_questions")
    @Builder.Default
    private Boolean melangerQuestions = false;

    @Column(name = "melanger_reponses")
    @Builder.Default
    private Boolean melangerReponses = false;

    @Column(name = "afficher_resultats")
    @Builder.Default
    private Boolean afficherResultats = true;

    @Column(name = "tentatives_max")
    @Builder.Default
    private Integer tentativesMax = 1;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(length = 20)
    @Builder.Default
    private String statut = "BROUILLON";

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
