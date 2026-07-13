package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflitDTO {
    private String typeConflit; // ENSEIGNANT, SALLE
    private Integer jourSemaine;
    private UUID creneauId;
    private UUID enseignantId;
    private String salle;
    private String message;
}
