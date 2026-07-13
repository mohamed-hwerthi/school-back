package com.schoolSys.schooolSys.finance;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "mouvements_caisse")
@SQLDelete(sql = "UPDATE mouvements_caisse SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MouvementCaisse {

    public enum TypeMouvement { ENTREE, SORTIE }
    public enum CategorieMouvement {
        PAIEMENT_SCOLARITE, INSCRIPTION, CANTINE, TRANSPORT,
        FOURNITURES, SALAIRE, FACTURE, MAINTENANCE, AUTRE
    }

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caisse_id", nullable = false)
    private Caisse caisse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TypeMouvement type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private CategorieMouvement categorie;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private String libelle;

    @Column(name = "reference_paiement")
    private String referencePaiement;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
