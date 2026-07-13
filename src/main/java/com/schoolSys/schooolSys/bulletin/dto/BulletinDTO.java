package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinDTO {
    private UUID studentId;
    private String matricule;
    private String studentName;
    private String studentNameAr;
    private String classe;
    private String niveau;
    private Integer trimestre;
    private String version; // "etatique" or "prive"

    private List<BulletinDomaineDTO> domaines;

    // modules without a domaine (fallback group)
    private List<BulletinModuleDTO> modulesHorsDomaine;

    private Double moyenneGenerale;
    private Double moyenneClasse;
    private Double moyenneMin;
    private Double moyenneMax;
    private Integer rang;
    private Integer totalEleves;

    private String certificatType;
    private String comportement;
}
