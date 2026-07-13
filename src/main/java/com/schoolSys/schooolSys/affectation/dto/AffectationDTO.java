package com.schoolSys.schooolSys.affectation.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffectationDTO {

    private UUID id;

    private UUID teacherId;
    private String teacherName;

    private UUID classeId;
    private String classeName;

    private UUID moduleId;
    private String moduleName;

    private String anneeScolaire;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String notes;
}
