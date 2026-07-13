package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.Relance;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RelanceResponseDTO {

    private UUID id;
    private UUID studentId;
    private String studentFirstName;
    private String studentLastName;
    private String studentClasse;
    private UUID paiementId;
    private String paiementReference;
    private Relance.TypeRelance type;
    private Relance.StatutRelance statut;
    private String message;
    private String destinataire;
    private BigDecimal montantDu;
    private LocalDate dateEnvoi;
    private LocalDate datePrevue;
    private String anneeScolaire;
    private Integer numeroRelance;
    private LocalDateTime createdAt;
}
