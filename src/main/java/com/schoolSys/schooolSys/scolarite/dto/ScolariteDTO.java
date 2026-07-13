package com.schoolSys.schooolSys.scolarite.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One historised year of a student's schooling (ANN-004). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScolariteDTO {

    private UUID id;
    private UUID studentId;
    private String studentName;
    private String anneeScolaire;
    private String niveau;
    private String classe;
    private String statut;
}
