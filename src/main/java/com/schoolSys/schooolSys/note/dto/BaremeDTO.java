package com.schoolSys.schooolSys.note.dto;

import java.util.UUID;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BaremeDTO {
    private UUID id;
    private String label;
    private BigDecimal noteMax;
    private BigDecimal noteMin;
    private BigDecimal notePassage;
    private Boolean actif;
}
