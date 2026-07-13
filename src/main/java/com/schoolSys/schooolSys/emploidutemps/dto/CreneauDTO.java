package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreneauDTO {
    private UUID id;
    private String label;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String type;
}
