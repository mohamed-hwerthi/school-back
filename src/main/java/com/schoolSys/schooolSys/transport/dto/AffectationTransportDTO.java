package com.schoolSys.schooolSys.transport.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffectationTransportDTO {
    private UUID id;
    private UUID eleveId;
    private UUID circuitId;
    private String circuitNom;
    private UUID arretId;
    private String arretNom;
    private String anneeScolaire;
    private Boolean actif;
    private LocalDateTime createdAt;
}
