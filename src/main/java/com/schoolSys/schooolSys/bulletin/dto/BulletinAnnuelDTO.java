package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Annual bulletin for one student: a synthesis of the three trimestre
 * bulletins — per-module and general annual averages (ANN-040).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinAnnuelDTO {

    private UUID studentId;
    private String studentName;
    private String studentNameAr;
    private String classe;
    private String niveau;
    private String version;

    /** General average per trimestre — {@code null} when the trimestre has no grades. */
    private Double moyenneT1;
    private Double moyenneT2;
    private Double moyenneT3;

    /** Mean of the available trimestre averages. */
    private Double moyenneAnnuelle;

    private Integer rang;
    private Integer totalEleves;

    /** Honour-roll mention derived from the annual average. */
    private String mention;

    private List<ModuleAnnuelDTO> modules;
}
