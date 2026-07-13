package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PenaliteResponseDTO {

    private UUID id;
    private UUID studentId;
    private String studentFirstName;
    private String studentLastName;
    private UUID paiementId;
    private String paiementReference;
    private BigDecimal montant;
    private String motif;
    private LocalDate dateApplication;
    private String anneeScolaire;
    private Boolean payee;
    private LocalDateTime createdAt;
}
