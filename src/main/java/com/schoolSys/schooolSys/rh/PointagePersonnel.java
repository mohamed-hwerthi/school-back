package com.schoolSys.schooolSys.rh;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "pointage_personnel")
@SQLDelete(sql = "UPDATE pointage_personnel SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointagePersonnel {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "employe_id", nullable = false)
    private UUID employeId;

    @Column(name = "employe_type", nullable = false, length = 20)
    private String employeType;

    @Column(name = "date_pointage", nullable = false)
    @Builder.Default
    private LocalDate datePointage = LocalDate.now();

    @Column(name = "heure_arrivee")
    private LocalTime heureArrivee;

    @Column(name = "heure_depart")
    private LocalTime heureDepart;

    @Column(name = "heures_travaillees", precision = 4, scale = 2)
    private BigDecimal heuresTravaillees;

    @Column(length = 20)
    @Builder.Default
    private String statut = "PRESENT";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
