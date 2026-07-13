package com.schoolSys.schooolSys.appreciation.dto;

import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkObservationRequestDTO {
    @Valid
    private List<ObservationRequestDTO> items;
}
