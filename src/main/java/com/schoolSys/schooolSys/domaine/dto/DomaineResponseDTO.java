package com.schoolSys.schooolSys.domaine.dto;

import java.util.UUID;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomaineResponseDTO {
    private UUID id;
    private String name;
    private String nameAr;
    private Integer ordre;
    private Double coeffEtatique;
    private Double coeffPrive;
    private Boolean versionEtatique;
    private Boolean versionPrivee;
    private UUID niveauId;
    private String niveauName;
    private List<SousDomaineDTO> sousDomaines;
}
