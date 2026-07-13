package com.schoolSys.schooolSys.cantine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {
    @NotNull
    private LocalDate dateMenu;
    @NotBlank
    private String jourSemaine;
    private String entree;
    @NotBlank
    private String platPrincipal;
    private String accompagnement;
    private String dessert;
    private List<String> allergenes;
    private String typeRegime;
    private Integer semaine;
    private String imageUrl;
}
