package com.schoolSys.schooolSys.student.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassageDTO {

    private UUID id;
    private UUID studentId;
    private String studentName;
    private String ancienNiveau;
    private String nouveauNiveau;
    private String ancienneClasse;
    private String nouvelleClasse;
    private String decision;
    private String anneeScolaire;
    private String motif;
    private String createdAt;
}
