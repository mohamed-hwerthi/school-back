package com.schoolSys.schooolSys.analytics.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class KpiConfigResponseDTO {

    private UUID id;
    private String nom;
    private String description;
    private String type;
    private BigDecimal valeurCible;
    private BigDecimal seuilAlerte;
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
