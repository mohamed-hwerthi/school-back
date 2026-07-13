package com.schoolSys.schooolSys.devoir.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionRequest {

    @NotNull(message = "La note est obligatoire")
    private BigDecimal note;

    private String commentaire;
}
