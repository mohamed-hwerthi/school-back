package com.schoolSys.schooolSys.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FinanceDashboardDTO {

    private BigDecimal totalEncaisse;
    private BigDecimal totalDu;
    private BigDecimal totalImpayes;
    private BigDecimal tauxRecouvrement;
    private long totalPaiements;
    private long paiementsPayes;
    private long paiementsEnRetard;
    private long paiementsPartiels;
    private long paiementsEnAttente;
}
