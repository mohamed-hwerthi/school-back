package com.schoolSys.schooolSys.note.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoyenneDTO {

    private UUID studentId;
    private String studentName;
    private Integer trimestre;
    private Map<String, Double> moyennesParModule;
    private Double moyenneGenerale;
}
