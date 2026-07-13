package com.schoolSys.schooolSys.examenonline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStatsDTO {

    private long totalTentatives;
    private BigDecimal moyenneScore;
    private double tauxReussite;
    private Map<String, Long> distributionNotes;
}
