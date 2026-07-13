package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Annual success statistics for one subject across a class (ANN-025). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatiereStatDTO {

    private UUID moduleId;
    private String moduleName;

    /** Class average of the annual module averages. */
    private double moyenne;

    private int reussis;
    private int echoues;

    /** Success rate (annual module average >= 10), as a percentage. */
    private double taux;
}
