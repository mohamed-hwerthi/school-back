package com.schoolSys.schooolSys.domaine;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import com.schoolSys.schooolSys.niveau.Niveau;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "domaines")
@SQLDelete(sql = "UPDATE domaines SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Domaine {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_ar")
    private String nameAr;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordre = 1;

    @Column(name = "coeff_etatique", nullable = false)
    @Builder.Default
    private Double coeffEtatique = 1.0;

    @Column(name = "coeff_prive", nullable = false)
    @Builder.Default
    private Double coeffPrive = 1.0;

    @Column(name = "version_etatique", nullable = false)
    @Builder.Default
    private Boolean versionEtatique = true;

    @Column(name = "version_privee", nullable = false)
    @Builder.Default
    private Boolean versionPrivee = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "niveau_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Niveau niveau;

    @OneToMany(mappedBy = "domaine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SousDomaine> sousDomaines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
