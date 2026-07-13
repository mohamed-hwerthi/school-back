package com.schoolSys.schooolSys.examenonline;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@SQLDelete(sql = "UPDATE questions SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texte;

    @Column(name = "type_question", nullable = false, length = 20)
    private String typeQuestion;

    @Column(precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal points = BigDecimal.ONE;

    @Column(nullable = false)
    private Integer ordre;

    @Column(columnDefinition = "TEXT")
    private String explication;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column
    @Builder.Default
    private Boolean obligatoire = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<ChoixReponse> choix = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
