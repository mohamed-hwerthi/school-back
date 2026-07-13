package com.schoolSys.schooolSys.depense.dto;

import com.schoolSys.schooolSys.depense.CategorieDepense;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategorieDepenseRequestDTO {

    @NotBlank(message = "Le nom est requis")
    private String nom;

    private CategorieDepense.TypeCategorie type;

    private String description;
}
