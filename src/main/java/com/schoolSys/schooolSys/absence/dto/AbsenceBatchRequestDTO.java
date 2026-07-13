package com.schoolSys.schooolSys.absence.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AbsenceBatchRequestDTO {
    @NotEmpty
    @Valid
    private List<AbsenceRequestDTO> absences;
}
