package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Per-module annual synthesis: the three trimestre averages and their mean (ANN-040). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAnnuelDTO {

    private UUID moduleId;
    private String moduleName;
    private Double moyenneT1;
    private Double moyenneT2;
    private Double moyenneT3;
    private Double moyenneAnnuelle;
}
