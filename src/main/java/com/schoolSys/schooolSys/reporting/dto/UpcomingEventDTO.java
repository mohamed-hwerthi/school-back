package com.schoolSys.schooolSys.reporting.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingEventDTO {
    private UUID id;
    private String titre;
    private LocalDate dateDebut;
    private String couleur;
    private String type;
    private String lieu;
}
