package com.schoolSys.schooolSys.depense.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DepenseStatsDTO {

    private BigDecimal totalDepenses;
    private long nombreDepenses;
    private List<CategorieTotal> parCategorie;

    public record CategorieTotal(UUID categorieId, String categorieNom, BigDecimal total) {}
}
