package com.schoolSys.schooolSys.devoir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevoirStatsDTO {

    private long totalDevoirs;
    private long totalSoumissions;
    private double tauxSoumission;
    private BigDecimal moyenneNotes;
    private long enRetard;
}
