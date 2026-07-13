package com.schoolSys.schooolSys.transport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportStatsDTO {
    private long totalCircuits;
    private long totalVehicules;
    private long totalEleves;
    private double tauxRemplissage;
}
