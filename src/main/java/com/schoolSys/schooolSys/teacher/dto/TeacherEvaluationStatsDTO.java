package com.schoolSys.schooolSys.teacher.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEvaluationStatsDTO {

    private UUID teacherId;
    private String teacherName;
    private double avgPonctualite;
    private double avgPedagogie;
    private double avgDiscipline;
    private double avgCommunication;
    private double avgImplication;
    private double avgGlobale;
    private int totalEvaluations;
}
