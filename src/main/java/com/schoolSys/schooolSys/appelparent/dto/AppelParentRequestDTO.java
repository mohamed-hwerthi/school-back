package com.schoolSys.schooolSys.appelparent.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppelParentRequestDTO {

    @NotNull(message = "L'élève est obligatoire")
    private UUID eleveId;

    @Size(max = 150)
    private String appelePar;

    @Size(max = 30)
    private String telephone;

    @Size(max = 100)
    private String motif;

    @NotBlank(message = "Les notes sont obligatoires")
    private String notes;

    /** Optional: defaults to now() if omitted. */
    private LocalDateTime dateAppel;
}
