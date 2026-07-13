package com.schoolSys.schooolSys.module;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import com.schoolSys.schooolSys.domaine.Domaine;
import com.schoolSys.schooolSys.domaine.SousDomaine;
import com.schoolSys.schooolSys.niveau.Niveau;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "modules")
@SQLDelete(sql = "UPDATE modules SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Module {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_vp")
    private String nameVp;

    @Column(name = "name_ar")
    private String nameAr;

    @Column(name = "coeff_etatique", nullable = false)
    @Builder.Default
    private Double coeffEtatique = 1.0;

    @Column(name = "coeff_prive", nullable = false)
    @Builder.Default
    private Double coeffPrive = 1.0;

    @Column(name = "ordre_etatique", nullable = false)
    @Builder.Default
    private Integer ordreEtatique = 1;

    @Column(name = "ordre_prive", nullable = false)
    @Builder.Default
    private Integer ordrePrive = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "niveau_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Niveau niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domaine_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Domaine domaine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sous_domaine_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SousDomaine sousDomaine;

    @Column(name = "version_etatique", nullable = false)
    @Builder.Default
    private Boolean versionEtatique = true;

    @Column(name = "version_privee", nullable = false)
    @Builder.Default
    private Boolean versionPrivee = true;

    @Column(name = "salle_type_requise", nullable = false, length = 30)
    @Builder.Default
    private String salleTypeRequise = "NORMAL";

    @Column(name = "duree_min_seance", nullable = false)
    @Builder.Default
    private Integer dureeMinSeance = 1;

    @Column(name = "duree_max_seance", nullable = false)
    @Builder.Default
    private Integer dureeMaxSeance = 2;

    @Column(name = "preference_horaire", nullable = false, length = 20)
    @Builder.Default
    private String preferenceHoraire = "INDIFFERENT";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
