package com.schoolSys.schooolSys.analytics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CohorteDTO {

    private String anneeScolaire;
    private long effectifInitial;
    private long effectifFinal;
    private double tauxRetention;
    private double moyenneGenerale;
}
