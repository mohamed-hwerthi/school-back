package com.schoolSys.schooolSys.cantine.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointageRepasDTO {
    private UUID id;
    private UUID eleveId;
    private LocalDate dateRepas;
    private String typeRepas;
    private Boolean present;
    private LocalDateTime createdAt;
}
