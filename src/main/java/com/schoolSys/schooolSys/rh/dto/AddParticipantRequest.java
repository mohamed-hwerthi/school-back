package com.schoolSys.schooolSys.rh.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddParticipantRequest {
    @NotNull
    private UUID employeId;
    @NotNull
    private String employeType;
    private Boolean present;
    private Boolean certificatObtenu;
}
