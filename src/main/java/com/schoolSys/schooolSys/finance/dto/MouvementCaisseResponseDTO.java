package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.finance.MouvementCaisse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MouvementCaisseResponseDTO {
    private UUID id;
    private UUID caisseId;
    private MouvementCaisse.TypeMouvement type;
    private MouvementCaisse.CategorieMouvement categorie;
    private BigDecimal montant;
    private String libelle;
    private String referencePaiement;
    private String notes;
    private LocalDateTime createdAt;
}
