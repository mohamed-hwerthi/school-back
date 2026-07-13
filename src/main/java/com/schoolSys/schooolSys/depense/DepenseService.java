package com.schoolSys.schooolSys.depense;

import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.depense.dto.DepenseRequestDTO;
import com.schoolSys.schooolSys.depense.dto.DepenseResponseDTO;
import com.schoolSys.schooolSys.depense.dto.DepenseStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepenseService {

    private final DepenseRepository depenseRepository;
    private final DepenseMapper depenseMapper;
    private final CategorieDepenseRepository categorieRepository;

    public PagedResponse<DepenseResponseDTO> findAll(
            String search,
            String anneeScolaire,
            UUID categorieId,
            Depense.ModePaiement modePaiement,
            Boolean recurrente,
            Pageable pageable
    ) {
        Specification<Depense> spec = Specification
                .where(DepenseSpecification.search(search))
                .and(DepenseSpecification.hasAnneeScolaire(anneeScolaire))
                .and(DepenseSpecification.hasCategorieId(categorieId))
                .and(DepenseSpecification.hasModePaiement(modePaiement))
                .and(DepenseSpecification.isRecurrente(recurrente));

        Page<Depense> page = depenseRepository.findAll(spec, pageable);
        List<DepenseResponseDTO> content = depenseMapper.toResponseDTOList(page.getContent());
        return PagedResponse.from(page, content);
    }

    public DepenseResponseDTO findById(UUID id) {
        Depense depense = depenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depense", id));
        return depenseMapper.toResponseDTO(depense);
    }

    @Transactional
    public DepenseResponseDTO create(DepenseRequestDTO dto) {
        CategorieDepense categorie = categorieRepository.findById(dto.getCategorieId())
                .orElseThrow(() -> new ResourceNotFoundException("CategorieDepense", dto.getCategorieId()));

        Depense depense = Depense.builder()
                .categorie(categorie)
                .libelle(dto.getLibelle())
                .montant(dto.getMontant())
                .dateDepense(dto.getDateDepense())
                .modePaiement(dto.getModePaiement())
                .fournisseur(dto.getFournisseur())
                .reference(dto.getReference() != null ? dto.getReference() : generateReference())
                .recurrente(dto.getRecurrente() != null ? dto.getRecurrente() : false)
                .notes(dto.getNotes())
                .anneeScolaire(dto.getAnneeScolaire())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return depenseMapper.toResponseDTO(depenseRepository.save(depense));
    }

    @Transactional
    public DepenseResponseDTO update(UUID id, DepenseRequestDTO dto) {
        Depense depense = depenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Depense", id));

        if (dto.getCategorieId() != null) {
            CategorieDepense categorie = categorieRepository.findById(dto.getCategorieId())
                    .orElseThrow(() -> new ResourceNotFoundException("CategorieDepense", dto.getCategorieId()));
            depense.setCategorie(categorie);
        }

        depense.setLibelle(dto.getLibelle());
        depense.setMontant(dto.getMontant());
        depense.setDateDepense(dto.getDateDepense());
        depense.setModePaiement(dto.getModePaiement());
        depense.setFournisseur(dto.getFournisseur());
        depense.setRecurrente(dto.getRecurrente() != null ? dto.getRecurrente() : depense.getRecurrente());
        depense.setNotes(dto.getNotes());
        depense.setAnneeScolaire(dto.getAnneeScolaire());

        return depenseMapper.toResponseDTO(depenseRepository.save(depense));
    }

    @Transactional
    public void delete(UUID id) {
        if (!depenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Depense", id);
        }
        depenseRepository.deleteById(id);
    }

    public DepenseStatsDTO getStats(String anneeScolaire) {
        BigDecimal totalDepenses = depenseRepository.sumMontantByAnneeScolaire(anneeScolaire);
        long count = depenseRepository.findByAnneeScolaire(anneeScolaire).size();

        List<DepenseStatsDTO.CategorieTotal> parCategorie = categorieRepository.findAll().stream()
                .map(cat -> {
                    BigDecimal total = depenseRepository.sumMontantByCategorieAndAnneeScolaire(cat.getId(), anneeScolaire);
                    return new DepenseStatsDTO.CategorieTotal(cat.getId(), cat.getNom(), total);
                })
                .filter(ct -> ct.total().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        return DepenseStatsDTO.builder()
                .totalDepenses(totalDepenses)
                .nombreDepenses(count)
                .parCategorie(parCategorie)
                .build();
    }

    private String generateReference() {
        return "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
