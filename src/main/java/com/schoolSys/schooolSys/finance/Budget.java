package com.schoolSys.schooolSys.finance;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "budgets")
@SQLDelete(sql = "UPDATE budgets SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "annee_scolaire", nullable = false)
    private String anneeScolaire;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, length = 20)
    private String type; // RECETTE, DEPENSE

    private String categorie;

    @Column(name = "montant_prevu")
    @Builder.Default
    private BigDecimal montantPrevu = BigDecimal.ZERO;

    @Column(name = "montant_realise")
    @Builder.Default
    private BigDecimal montantRealise = BigDecimal.ZERO;

    private Integer mois;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
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
