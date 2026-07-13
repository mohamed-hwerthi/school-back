package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinModuleDTO {
    private UUID moduleId;
    private String moduleName;
    private String moduleNameAr;
    private UUID sousDomaineId;
    private String sousDomaineName;
    private String sousDomaineNameAr;
    private Integer sousDomaineOrdre;
    private Double coeff;
    private Integer ordre;
    private List<BulletinExamenDTO> examens;
    private Double moyenneModule;
    // class-level stats for this module
    private Double moduleMin;
    private Double moduleMax;
    private Double moduleClasseAvg;
}
