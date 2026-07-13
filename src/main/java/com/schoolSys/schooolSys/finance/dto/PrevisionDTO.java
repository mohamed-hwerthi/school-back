package com.schoolSys.schooolSys.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrevisionDTO {

    private String anneeScolaire;
    private BigDecimal totalRecettesPrevues;
    private BigDecimal totalRecettesRealisees;
    private BigDecimal totalDepensesPrevues;
    private BigDecimal totalDepensesRealisees;
    private BigDecimal soldePrevu;
    private BigDecimal soldeRealise;
    private BigDecimal variance;
    private List<BudgetDTO> budgets;
}
