package com.schoolSys.schooolSys.appreciation.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationRequestDTO {

    @NotNull
    private UUID studentId;

    @NotNull
    private Integer trimestre;

    private String comportement;

    private String certificatType;
}
