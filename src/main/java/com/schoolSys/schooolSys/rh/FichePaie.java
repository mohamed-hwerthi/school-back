package com.schoolSys.schooolSys.rh;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "fiches_paie")
@SQLDelete(sql = "UPDATE fiches_paie SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichePaie {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "employe_id", nullable = false)
    private UUID employeId;

    @Column(name = "employe_type", nullable = false, length = 20)
    private String employeType;

    @Column(nullable = false)
    private Integer mois;

    @Column(nullable = false)
    private Integer annee;

    @Column(name = "salaire_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salaireBase;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal primes = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal retenues = BigDecimal.ZERO;

    @Column(name = "salaire_net", nullable = false, precision = 10, scale = 2)
    private BigDecimal salaireNet;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Builder.Default
    private Boolean paye = false;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
