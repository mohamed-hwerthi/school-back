package com.schoolSys.schooolSys.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDTO {

    private String month;
    private long inscriptions;
    private BigDecimal paiements;
    private long absences;
}
