package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.finance.dto.PenaliteResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PenaliteMapper {

    public PenaliteResponseDTO toResponseDTO(Penalite penalite) {
        return PenaliteResponseDTO.builder()
                .id(penalite.getId())
                .studentId(penalite.getStudent().getId())
                .studentFirstName(penalite.getStudent().getFirstName())
                .studentLastName(penalite.getStudent().getLastName())
                .paiementId(penalite.getPaiement() != null ? penalite.getPaiement().getId() : null)
                .paiementReference(penalite.getPaiement() != null ? penalite.getPaiement().getReference() : null)
                .montant(penalite.getMontant())
                .motif(penalite.getMotif())
                .dateApplication(penalite.getDateApplication())
                .anneeScolaire(penalite.getAnneeScolaire())
                .payee(penalite.getPayee())
                .createdAt(penalite.getCreatedAt())
                .build();
    }

    public List<PenaliteResponseDTO> toResponseDTOList(List<Penalite> penalites) {
        return penalites.stream().map(this::toResponseDTO).toList();
    }
}
