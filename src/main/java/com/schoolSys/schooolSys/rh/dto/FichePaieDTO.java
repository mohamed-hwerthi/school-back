package com.schoolSys.schooolSys.rh.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FichePaieDTO {
    private UUID id;
    private UUID employeId;
    private String employeType;
    private Integer mois;
    private Integer annee;
    private BigDecimal salaireBase;
    private BigDecimal primes;
    private BigDecimal retenues;
    private BigDecimal salaireNet;
    private LocalDate datePaiement;
    private Boolean paye;
    private String commentaire;
    private LocalDateTime createdAt;
}
