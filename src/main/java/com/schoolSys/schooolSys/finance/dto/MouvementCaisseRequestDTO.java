package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.MouvementCaisse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MouvementCaisseRequestDTO {

    @NotNull
    private UUID caisseId;

    @NotNull
    private MouvementCaisse.TypeMouvement type;

    @NotNull
    private MouvementCaisse.CategorieMouvement categorie;

    @NotNull
    @Positive
    private BigDecimal montant;

    @NotBlank
    private String libelle;

    private String referencePaiement;
    private String notes;
}
