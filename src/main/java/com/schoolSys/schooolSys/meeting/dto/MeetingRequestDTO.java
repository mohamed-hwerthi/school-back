package com.schoolSys.schooolSys.meeting.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRequestDTO {

    @NotBlank(message = "Le titre est requis")
    private String title;

    @NotNull(message = "La date est requise")
    private LocalDate date;

    @NotNull(message = "L'heure de debut est requise")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est requise")
    private LocalTime heureFin;

    private UUID enseignantId;

    private UUID parentId;

    private UUID studentId;

    private String statut;

    private String notes;
}
