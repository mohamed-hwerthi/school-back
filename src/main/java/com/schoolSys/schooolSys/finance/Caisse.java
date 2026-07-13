package com.schoolSys.schooolSys.finance;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "caisses")
@SQLDelete(sql = "UPDATE caisses SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Caisse {

    public enum StatutCaisse { OUVERTE, FERMEE }

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDate dateOuverture;

    @Column(name = "date_fermeture")
    private LocalDate dateFermeture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutCaisse statut;

    @Column(name = "solde_ouverture", nullable = false, precision = 10, scale = 2)
    private BigDecimal soldeOuverture;

    @Column(name = "solde_fermeture", precision = 10, scale = 2)
    private BigDecimal soldeFermeture;

    @Column(name = "total_entrees", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEntrees;

    @Column(name = "total_sorties", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalSorties;

    @Column(name = "annee_scolaire", nullable = false, length = 9)
    private String anneeScolaire;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ouvert_par")
    private String ouvertPar;

    @Column(name = "ferme_par")
    private String fermePar;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
