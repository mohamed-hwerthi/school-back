package com.schoolSys.schooolSys.module.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponseDTO {

    private UUID id;
    private String name;
    private String nameVp;
    private String nameAr;
    private Double coeffEtatique;
    private Double coeffPrive;
    private Integer ordreEtatique;
    private Integer ordrePrive;
    private UUID niveauId;
    private String niveauName;
    private UUID domaineId;
    private String domaineName;
    private UUID sousDomaineId;
    private String sousDomaineName;
    private Boolean versionEtatique;
    private Boolean versionPrivee;
    private String salleTypeRequise;
    private Integer dureeMinSeance;
    private Integer dureeMaxSeance;
    private String preferenceHoraire;
}
