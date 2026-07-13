package com.schoolSys.schooolSys.absence.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceResponseDTO {
    private UUID id;
    private UUID eleveId;
    private LocalDate date;
    private String type;
    private String seance;
    private LocalTime heureArrivee;
    private Boolean justifie;
    private String motif;
    private UUID enseignantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
