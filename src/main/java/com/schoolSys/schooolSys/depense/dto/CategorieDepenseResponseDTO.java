package com.schoolSys.schooolSys.depense.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.depense.CategorieDepense;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategorieDepenseResponseDTO {

    private UUID id;
    private String nom;
    private CategorieDepense.TypeCategorie type;
    private String description;
    private LocalDateTime createdAt;
}
