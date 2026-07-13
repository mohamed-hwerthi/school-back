package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.Remise;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RemiseResponseDTO {

    private UUID id;
    private UUID studentId;
    private String studentFirstName;
    private String studentLastName;
    private UUID typeFraisId;
    private String typeFraisNom;
    private Remise.TypeRemise type;
    private BigDecimal valeur;
    private Boolean estPourcentage;
    private String motif;
    private String anneeScolaire;
    private Boolean active;
    private LocalDateTime createdAt;
}
