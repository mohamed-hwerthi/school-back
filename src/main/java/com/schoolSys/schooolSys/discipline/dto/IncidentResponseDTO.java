package com.schoolSys.schooolSys.discipline.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponseDTO {
    private UUID id;
    private String titre;
    private String description;
    private LocalDate date;
    private String type;
    private String gravite;
    private String lieu;
    private UUID signaleParId;
    private List<IncidentEleveResponseDTO> elevesImpliques;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentEleveResponseDTO {
        private UUID id;
        private UUID eleveId;
        private String roleEleve;
    }
}
