package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "emploi_du_temps")
@SQLDelete(sql = "UPDATE emploi_du_temps SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploiDuTemps {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "classe_id", nullable = false)
    private UUID classeId;

    @Column(name = "creneau_id", nullable = false)
    private UUID creneauId;

    @Column(name = "jour_semaine", nullable = false)
    private Integer jourSemaine; // 1=Monday ... 7=Sunday

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "enseignant_id")
    private UUID enseignantId;

    @Column(length = 100)
    private String salle;

    @Column(name = "classroom_id")
    private UUID classroomId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
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
