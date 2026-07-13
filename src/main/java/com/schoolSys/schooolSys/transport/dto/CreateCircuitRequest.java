package com.schoolSys.schooolSys.transport.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCircuitRequest {
    @NotBlank
    private String nom;
    private String description;
    private UUID vehiculeId;
    private LocalTime heureDepart;
    private LocalTime heureRetour;
    private BigDecimal distanceKm;
    private BigDecimal coutMensuel;
    private List<ArretDTO> arrets;
}
