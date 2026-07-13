package com.schoolSys.schooolSys.rh;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Table(name = "formation_participants")
@SQLDelete(sql = "UPDATE formation_participants SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormationParticipant {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;

    @Column(name = "employe_id", nullable = false)
    private UUID employeId;

    @Column(name = "employe_type", nullable = false, length = 20)
    private String employeType;

    @Builder.Default
    private Boolean present = false;

    @Column(name = "certificat_obtenu")
    @Builder.Default
    private Boolean certificatObtenu = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
