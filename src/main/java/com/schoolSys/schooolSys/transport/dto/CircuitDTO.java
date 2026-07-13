package com.schoolSys.schooolSys.transport.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircuitDTO {
    private UUID id;
    private String nom;
    private String description;
    private UUID vehiculeId;
    private String vehiculeImmatriculation;
    private LocalTime heureDepart;
    private LocalTime heureRetour;
    private BigDecimal distanceKm;
    private BigDecimal coutMensuel;
    private Boolean actif;
    private List<ArretDTO> arrets;
    private Long nbEleves;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
