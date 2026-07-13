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
public class NiveauRequestDTO {

    @NotBlank(message = "Le nom du niveau est obligatoire")
    @Size(max = 100)
    private String name;
}
