package com.schoolSys.schooolSys.examen.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenResponseDTO {

    private UUID id;
    private String name;
    private String namePrive;
    private Double coeffEtatique;
    private Double coeffPrive;
    private Integer ordreEtatique;
    private Integer ordrePrive;
    private Integer trimestre;
    private UUID classeId;
    private String classeName;
    private UUID teacherId;
    private String teacherName;
    private UUID moduleId;
    private String moduleName;
    private Boolean versionEtatique;
    private Boolean versionPrivee;
    private String anneeScolaire;
    private Long nbNotes;
    private Long nbEleves;
}
