package com.schoolSys.schooolSys.finance;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import com.schoolSys.schooolSys.student.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "relances")
@SQLDelete(sql = "UPDATE relances SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Relance {

    public enum TypeRelance { EMAIL, SMS, COURRIER }
    public enum StatutRelance { EN_ATTENTE, ENVOYEE, ECHOUEE }

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paiement_id")
    private Paiement paiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeRelance type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatutRelance statut;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "destinataire", nullable = false)
    private String destinataire;

    @Column(name = "montant_du", precision = 10, scale = 2)
    private BigDecimal montantDu;

    @Column(name = "date_envoi")
    private LocalDate dateEnvoi;

    @Column(name = "date_prevue", nullable = false)
    private LocalDate datePrevue;

    @Column(name = "annee_scolaire", nullable = false, length = 9)
    private String anneeScolaire;

    @Column(name = "numero_relance", nullable = false)
    private Integer numeroRelance;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
