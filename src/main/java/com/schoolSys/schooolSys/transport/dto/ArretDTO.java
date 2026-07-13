package com.schoolSys.schooolSys.transport.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArretDTO {
    private UUID id;
    private UUID circuitId;
    private String nom;
    private String adresse;
    private Integer ordre;
    private LocalTime heurePassage;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
