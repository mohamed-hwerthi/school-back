package com.schoolSys.schooolSys.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compteurs d'alertes affichés sur le home parent (badge rouge / orange).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertsDTO {

    private Integer absencesNonJustifiees;
    private Integer retards;
    private Integer incidentsRecents;
}
