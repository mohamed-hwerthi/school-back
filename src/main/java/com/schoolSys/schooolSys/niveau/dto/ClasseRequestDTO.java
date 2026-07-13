package com.schoolSys.schooolSys.niveau.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseRequestDTO {

    @NotBlank(message = "La lettre de la classe est obligatoire")
    @Size(max = 5)
    private String letter;
}
