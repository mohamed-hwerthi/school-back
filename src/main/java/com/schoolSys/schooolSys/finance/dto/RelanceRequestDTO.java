package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.Relance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RelanceRequestDTO {

    @NotNull
    private UUID studentId;

    private UUID paiementId;

    @NotNull
    private Relance.TypeRelance type;

    @NotBlank
    private String message;

    private String destinataire;

    private BigDecimal montantDu;

    private LocalDate datePrevue;

    private String anneeScolaire;

    private Integer numeroRelance;
}
