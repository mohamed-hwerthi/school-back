package com.schoolSys.schooolSys.depense.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.depense.Depense;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DepenseRequestDTO {

    @NotNull(message = "La catégorie est requise")
    private UUID categorieId;

    @NotBlank(message = "Le libellé est requis")
    private String libelle;

    @NotNull(message = "Le montant est requis")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "La date est requise")
    private LocalDate dateDepense;

    private Depense.ModePaiement modePaiement;

    private String fournisseur;

    private String reference;

    private Boolean recurrente;

    private String notes;

    @NotBlank(message = "L'année scolaire est requise")
    private String anneeScolaire;
}
