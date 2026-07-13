package com.schoolSys.schooolSys.emploidutemps.solver;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningRoom {

    private Long id;
    private String name;

    /** Optional FK to classroom DB row (null when room is a free-form string from legacy mode). */
    private UUID classroomId;

    /** Capacity in students; null = unlimited (legacy free-form rooms). */
    private Integer capacite;

    /** Room type (NORMAL, LABO_SVT, LABO_PHYSIQUE, INFO, GYMNASE, ARTS, MUSIQUE). */
    private String type;
}
