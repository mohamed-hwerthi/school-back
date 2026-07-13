package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinDomaineDTO {
    private UUID domaineId;
    private String domaineName;
    private String domaineNameAr;
    private Integer ordre;
    private Double coeff;
    private List<BulletinModuleDTO> modules;
    private Double moyenneDomaine;
    private String recommandation;
}
