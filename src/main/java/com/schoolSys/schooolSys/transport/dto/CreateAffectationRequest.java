package com.schoolSys.schooolSys.transport.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAffectationRequest {
    @NotNull
    private UUID eleveId;
    @NotNull
    private UUID circuitId;
    private UUID arretId;
    @NotNull
    private String anneeScolaire;
}
