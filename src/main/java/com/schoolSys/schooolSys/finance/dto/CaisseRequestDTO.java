package com.schoolSys.schooolSys.finance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CaisseRequestDTO {

    @NotNull
    @PositiveOrZero
    private BigDecimal soldeOuverture;

    private String anneeScolaire;
    private String notes;
    private String ouvertPar;
}
