package com.schoolSys.schooolSys.analytics.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KpiConfigRequestDTO {

    private String nom;
    private String description;
    private String type;
    private BigDecimal valeurCible;
    private BigDecimal seuilAlerte;
    private Boolean actif;
}
