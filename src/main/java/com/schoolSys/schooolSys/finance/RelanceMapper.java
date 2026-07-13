package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.finance.dto.RelanceResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RelanceMapper {

    public RelanceResponseDTO toResponseDTO(Relance r) {
        if (r == null) return null;

        return RelanceResponseDTO.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .studentFirstName(r.getStudent().getFirstName())
                .studentLastName(r.getStudent().getLastName())
                .studentClasse(r.getStudent().getClasse())
                .paiementId(r.getPaiement() != null ? r.getPaiement().getId() : null)
                .paiementReference(r.getPaiement() != null ? r.getPaiement().getReference() : null)
                .type(r.getType())
                .statut(r.getStatut())
                .message(r.getMessage())
                .destinataire(r.getDestinataire())
                .montantDu(r.getMontantDu())
                .dateEnvoi(r.getDateEnvoi())
                .datePrevue(r.getDatePrevue())
                .anneeScolaire(r.getAnneeScolaire())
                .numeroRelance(r.getNumeroRelance())
                .createdAt(r.getCreatedAt())
                .build();
    }

    public List<RelanceResponseDTO> toResponseDTOList(List<Relance> list) {
        return list.stream().map(this::toResponseDTO).toList();
    }
}
