package com.schoolSys.schooolSys.note.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponseDTO {

    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID examenId;
    private String examenName;
    private Integer trimestre;
    private Double valeur;
    private String observation;
    private String statut;
}
