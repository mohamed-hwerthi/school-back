package com.schoolSys.schooolSys.rh.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationParticipantDTO {
    private UUID id;
    private UUID formationId;
    private UUID employeId;
    private String employeType;
    private Boolean present;
    private Boolean certificatObtenu;
}
