package com.schoolSys.schooolSys.depense.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.depense.Depense;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DepenseResponseDTO {

    private UUID id;
    private UUID categorieId;
    private String categorieNom;
    private String libelle;
    private BigDecimal montant;
    private LocalDate dateDepense;
    private Depense.ModePaiement modePaiement;
    private String fournisseur;
    private String reference;
    private Boolean recurrente;
    private String notes;
    private String anneeScolaire;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
