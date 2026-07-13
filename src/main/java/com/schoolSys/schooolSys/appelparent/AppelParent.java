package com.schoolSys.schooolSys.appelparent;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "appels_parents")
@SQLDelete(sql = "UPDATE appels_parents SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppelParent {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(name = "eleve_id", nullable = false)
    private UUID eleveId;

    @Column(name = "appele_par", length = 150)
    private String appelePar;

    @Column(length = 30)
    private String telephone;

    @Column(length = 100)
    private String motif;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String notes;

    @Column(name = "date_appel", nullable = false)
    @Builder.Default
    private LocalDateTime dateAppel = LocalDateTime.now();

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
