package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.Paiement;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PaiementResponseDTO {

    private UUID id;
    private UUID studentId;
    private String studentFirstName;
    private String studentLastName;
    private UUID typeFraisId;
    private String typeFraisNom;
    private String mois;
    private String anneeScolaire;
    private BigDecimal montantDu;
    private BigDecimal montantPaye;
    private LocalDate datePaiement;
    private Paiement.ModePaiement modePaiement;
    private Paiement.StatutPaiement statut;
    private String reference;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
