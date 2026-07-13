package com.schoolSys.schooolSys.examen;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.teacher.Teacher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "examens")
@SQLDelete(sql = "UPDATE examens SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Examen {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_prive")
    private String namePrive;

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

    @Column(nullable = false)
    @Builder.Default
    private Integer trimestre = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Module module;

    @Column(name = "annee_scolaire", nullable = false)
    private String anneeScolaire;

    @Column(name = "date_limite_saisie")
    private LocalDate dateLimiteSaisie;

    @Column(name = "version_etatique", nullable = false)
    @Builder.Default
    private Boolean versionEtatique = true;

    @Column(name = "version_privee", nullable = false)
    @Builder.Default
    private Boolean versionPrivee = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
