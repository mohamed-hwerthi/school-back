package com.schoolSys.schooolSys.emploidutemps.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableGenerationResponseDTO {

    private String status; // SOLVED, INFEASIBLE
    private String score;  // e.g. "0hard/-3soft"
    private List<EmploiDuTempsResponseDTO> entries;
    private List<String> unresolvedConflicts;
}
