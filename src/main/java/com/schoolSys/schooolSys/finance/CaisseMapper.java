package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.finance.dto.CaisseResponseDTO;
import com.schoolSys.schooolSys.finance.dto.MouvementCaisseResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CaisseMapper {

    public CaisseResponseDTO toResponseDTO(Caisse c) {
        if (c == null) return null;
        BigDecimal soldeActuel = c.getSoldeOuverture()
                .add(c.getTotalEntrees())
                .subtract(c.getTotalSorties());

        return CaisseResponseDTO.builder()
                .id(c.getId())
                .dateOuverture(c.getDateOuverture())
                .dateFermeture(c.getDateFermeture())
                .statut(c.getStatut())
                .soldeOuverture(c.getSoldeOuverture())
                .soldeFermeture(c.getSoldeFermeture())
                .totalEntrees(c.getTotalEntrees())
                .totalSorties(c.getTotalSorties())
                .soldeActuel(soldeActuel)
                .anneeScolaire(c.getAnneeScolaire())
                .notes(c.getNotes())
                .ouvertPar(c.getOuvertPar())
                .fermePar(c.getFermePar())
                .createdAt(c.getCreatedAt())
                .build();
    }

    public List<CaisseResponseDTO> toResponseDTOList(List<Caisse> list) {
        return list.stream().map(this::toResponseDTO).toList();
    }

    public MouvementCaisseResponseDTO toMouvementDTO(MouvementCaisse m) {
        if (m == null) return null;
        return MouvementCaisseResponseDTO.builder()
                .id(m.getId())
                .caisseId(m.getCaisse().getId())
                .type(m.getType())
                .categorie(m.getCategorie())
                .montant(m.getMontant())
                .libelle(m.getLibelle())
                .referencePaiement(m.getReferencePaiement())
                .notes(m.getNotes())
                .createdAt(m.getCreatedAt())
                .build();
    }

    public List<MouvementCaisseResponseDTO> toMouvementDTOList(List<MouvementCaisse> list) {
        return list.stream().map(this::toMouvementDTO).toList();
    }
}
