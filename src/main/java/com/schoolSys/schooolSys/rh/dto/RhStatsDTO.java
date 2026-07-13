package com.schoolSys.schooolSys.rh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RhStatsDTO {
    private long totalEmployes;
    private BigDecimal masseSalariale;
    private long formationsEnCours;
    private double tauxPresence;
}
