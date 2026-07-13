package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaisseService {

    private final AnneeScolaireProvider anneeScolaireProvider;
    private final CaisseRepository caisseRepository;
    private final MouvementCaisseRepository mouvementCaisseRepository;
    private final CaisseMapper caisseMapper;

    public List<CaisseResponseDTO> findAll(String anneeScolaire) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return caisseMapper.toResponseDTOList(
                caisseRepository.findByAnneeScolaireOrderByCreatedAtDesc(annee));
    }

    public CaisseResponseDTO findById(UUID id) {
        Caisse c = caisseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caisse", id));
        return caisseMapper.toResponseDTO(c);
    }

    public CaisseResponseDTO findOuverte(String anneeScolaire) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        Caisse c = caisseRepository.findByStatutAndAnneeScolaire(Caisse.StatutCaisse.OUVERTE, annee)
                .orElse(null);
        return c != null ? caisseMapper.toResponseDTO(c) : null;
    }

    @Transactional
    public CaisseResponseDTO ouvrir(CaisseRequestDTO dto) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(dto.getAnneeScolaire());

        if (caisseRepository.existsByStatutAndAnneeScolaire(Caisse.StatutCaisse.OUVERTE, annee)) {
            throw new IllegalArgumentException("Une caisse est deja ouverte pour cette annee scolaire");
        }

        Caisse caisse = Caisse.builder()
                .dateOuverture(LocalDate.now())
                .statut(Caisse.StatutCaisse.OUVERTE)
                .soldeOuverture(dto.getSoldeOuverture())
                .totalEntrees(BigDecimal.ZERO)
                .totalSorties(BigDecimal.ZERO)
                .anneeScolaire(annee)
                .notes(dto.getNotes())
                .ouvertPar(dto.getOuvertPar())
                .build();

        return caisseMapper.toResponseDTO(caisseRepository.save(caisse));
    }

    @Transactional
    public CaisseResponseDTO fermer(UUID id, String fermePar) {
        Caisse c = caisseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caisse", id));

        if (c.getStatut() == Caisse.StatutCaisse.FERMEE) {
            throw new IllegalArgumentException("Cette caisse est deja fermee");
        }

        // Recalculate totals from movements
        BigDecimal entrees = mouvementCaisseRepository.sumEntreesByCaisseId(id);
        BigDecimal sorties = mouvementCaisseRepository.sumSortiesByCaisseId(id);
        c.setTotalEntrees(entrees);
        c.setTotalSorties(sorties);
        c.setSoldeFermeture(c.getSoldeOuverture().add(entrees).subtract(sorties));
        c.setDateFermeture(LocalDate.now());
        c.setStatut(Caisse.StatutCaisse.FERMEE);
        c.setFermePar(fermePar);

        return caisseMapper.toResponseDTO(caisseRepository.save(c));
    }

    // ── Mouvements ──

    public List<MouvementCaisseResponseDTO> findMouvements(UUID caisseId) {
        return caisseMapper.toMouvementDTOList(
                mouvementCaisseRepository.findByCaisseIdOrderByCreatedAtDesc(caisseId));
    }

    @Transactional
    public MouvementCaisseResponseDTO addMouvement(MouvementCaisseRequestDTO dto) {
        Caisse caisse = caisseRepository.findById(dto.getCaisseId())
                .orElseThrow(() -> new ResourceNotFoundException("Caisse", dto.getCaisseId()));

        if (caisse.getStatut() == Caisse.StatutCaisse.FERMEE) {
            throw new IllegalArgumentException("Impossible d'ajouter un mouvement a une caisse fermee");
        }

        MouvementCaisse mouvement = MouvementCaisse.builder()
                .caisse(caisse)
                .type(dto.getType())
                .categorie(dto.getCategorie())
                .montant(dto.getMontant())
                .libelle(dto.getLibelle())
                .referencePaiement(dto.getReferencePaiement())
                .notes(dto.getNotes())
                .build();

        mouvement = mouvementCaisseRepository.save(mouvement);

        // Update caisse totals
        if (dto.getType() == MouvementCaisse.TypeMouvement.ENTREE) {
            caisse.setTotalEntrees(caisse.getTotalEntrees().add(dto.getMontant()));
        } else {
            caisse.setTotalSorties(caisse.getTotalSorties().add(dto.getMontant()));
        }
        caisseRepository.save(caisse);

        return caisseMapper.toMouvementDTO(mouvement);
    }

    @Transactional
    public void deleteMouvement(UUID mouvementId) {
        MouvementCaisse m = mouvementCaisseRepository.findById(mouvementId)
                .orElseThrow(() -> new ResourceNotFoundException("Mouvement", mouvementId));

        Caisse caisse = m.getCaisse();
        if (caisse.getStatut() == Caisse.StatutCaisse.FERMEE) {
            throw new IllegalArgumentException("Impossible de supprimer un mouvement d'une caisse fermee");
        }

        // Update caisse totals
        if (m.getType() == MouvementCaisse.TypeMouvement.ENTREE) {
            caisse.setTotalEntrees(caisse.getTotalEntrees().subtract(m.getMontant()));
        } else {
            caisse.setTotalSorties(caisse.getTotalSorties().subtract(m.getMontant()));
        }
        caisseRepository.save(caisse);
        mouvementCaisseRepository.delete(m);
    }
}
