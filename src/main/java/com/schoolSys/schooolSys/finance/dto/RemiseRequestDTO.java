package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.Remise;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RemiseRequestDTO {

    @NotNull(message = "L'élève est requis")
    private UUID studentId;

    private UUID typeFraisId;

    @NotNull(message = "Le type est requis")
    private Remise.TypeRemise type;

    @NotNull(message = "La valeur est requise")
    @Positive(message = "La valeur doit être positive")
    private BigDecimal valeur;

    private Boolean estPourcentage;

    private String motif;

    private String anneeScolaire;

    private Boolean active;
}
