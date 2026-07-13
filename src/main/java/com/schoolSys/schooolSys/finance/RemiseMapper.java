package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.finance.dto.RemiseResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RemiseMapper {

    public RemiseResponseDTO toResponseDTO(Remise remise) {
        return RemiseResponseDTO.builder()
                .id(remise.getId())
                .studentId(remise.getStudent().getId())
                .studentFirstName(remise.getStudent().getFirstName())
                .studentLastName(remise.getStudent().getLastName())
                .typeFraisId(remise.getTypeFrais() != null ? remise.getTypeFrais().getId() : null)
                .typeFraisNom(remise.getTypeFrais() != null ? remise.getTypeFrais().getNom() : null)
                .type(remise.getType())
                .valeur(remise.getValeur())
                .estPourcentage(remise.getEstPourcentage())
                .motif(remise.getMotif())
                .anneeScolaire(remise.getAnneeScolaire())
                .active(remise.getActive())
                .createdAt(remise.getCreatedAt())
                .build();
    }

    public List<RemiseResponseDTO> toResponseDTOList(List<Remise> remises) {
        return remises.stream().map(this::toResponseDTO).toList();
    }
}
