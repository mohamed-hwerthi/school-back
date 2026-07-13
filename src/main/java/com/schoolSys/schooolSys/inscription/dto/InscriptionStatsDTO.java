package com.schoolSys.schooolSys.inscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionStatsDTO {

    private long totalSoumises;
    private long totalAcceptees;
    private long totalRefusees;
    private long totalEnAttente;
    private long totalListeAttente;
    private double tauxConversion;
}
