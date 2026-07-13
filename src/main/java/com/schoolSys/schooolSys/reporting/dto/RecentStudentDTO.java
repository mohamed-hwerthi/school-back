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
public class RecentStudentDTO {
    private UUID id;
    private String fullName;
    private String classe;
    private LocalDate enrollmentDate;
    private String statut;
}
