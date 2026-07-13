package com.schoolSys.schooolSys.note;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity @Table(name = "baremes")
@SQLDelete(sql = "UPDATE baremes SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Bareme {
    @Id @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "note_max", nullable = false)
    @Builder.Default
    private BigDecimal noteMax = new BigDecimal("20.0");

    @Column(name = "note_min", nullable = false)
    @Builder.Default
    private BigDecimal noteMin = BigDecimal.ZERO;

    @Column(name = "note_passage", nullable = false)
    @Builder.Default
    private BigDecimal notePassage = new BigDecimal("10.0");

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
