package com.schoolSys.schooolSys.emploidutemps.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAssignmentDTO {

    @NotNull
    private UUID classeId;

    @NotNull
    private UUID moduleId;

    @NotNull
    private UUID enseignantId;

    @NotNull
    @Min(1)
    private Integer nbHeures;
}
