package com.schoolSys.schooolSys.discipline.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class IncidentRequestDTO {
    @NotBlank
    private String titre;
    private String description;
    @NotNull
    private LocalDate date;
    private String type;
    private String gravite;
    private String lieu;
    private UUID signaleParId;
    private List<IncidentEleveDTO> elevesImpliques;

    @Data
    public static class IncidentEleveDTO {
        @NotNull
        private UUID eleveId;
        private String roleEleve;
    }
}
