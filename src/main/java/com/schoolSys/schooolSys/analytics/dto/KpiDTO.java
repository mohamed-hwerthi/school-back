package com.schoolSys.schooolSys.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class KpiDTO {

    private String nom;
    private String type;
    private BigDecimal valeurActuelle;
    private BigDecimal valeurCible;
    private BigDecimal seuilAlerte;
    private String statut;
    private String tendance;
}
