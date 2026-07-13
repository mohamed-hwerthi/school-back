package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.Paiement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaiementRequestDTO {

    @NotNull(message = "L'élève est requis")
    private UUID studentId;

    @NotNull(message = "Le type de frais est requis")
    private UUID typeFraisId;

    @NotBlank(message = "Le mois est requis")
    private String mois;

    @NotBlank(message = "L'année scolaire est requise")
    private String anneeScolaire;

    @NotNull(message = "Le montant dû est requis")
    @PositiveOrZero(message = "Le montant dû doit être positif ou zéro")
    private BigDecimal montantDu;

    @NotNull(message = "Le montant payé est requis")
    @PositiveOrZero(message = "Le montant payé doit être positif ou zéro")
    private BigDecimal montantPaye;

    private LocalDate datePaiement;

    private Paiement.ModePaiement modePaiement;

    private Paiement.StatutPaiement statut;

    private String reference;

    private String notes;
}
