package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    private UUID id;
    private String anneeScolaire;
    private String label;
    private String type;
    private String categorie;
    private BigDecimal montantPrevu;
    private BigDecimal montantRealise;
    private BigDecimal variance;
    private Integer mois;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
