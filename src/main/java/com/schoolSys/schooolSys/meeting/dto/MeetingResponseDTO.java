package com.schoolSys.schooolSys.meeting.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponseDTO {

    private UUID id;

    private String title;

    private LocalDate date;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    private UUID enseignantId;

    private String enseignantName;

    private UUID parentId;

    private String parentName;

    private UUID studentId;

    private String studentName;

    private String statut;

    private String notes;

    private LocalDateTime createdAt;
}
