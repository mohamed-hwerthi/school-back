package com.schoolSys.schooolSys.appreciation.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommandationRequestDTO {

    @NotNull
    private UUID studentId;

    @NotNull
    private UUID domaineId;

    @NotNull
    private Integer trimestre;

    private String texte;
}
