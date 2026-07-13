package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.BudgetDTO;
import com.schoolSys.schooolSys.finance.dto.PrevisionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public List<BudgetDTO> findAll(String anneeScolaire) {
        List<Budget> budgets;
        if (anneeScolaire != null && !anneeScolaire.isBlank()) {
            budgets = budgetRepository.findByAnneeScolaire(anneeScolaire);
        } else {
            budgets = budgetRepository.findAll();
        }
        return budgets.stream().map(this::toDTO).toList();
    }

    public BudgetDTO findById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        return toDTO(budget);
    }

    @Transactional
    public BudgetDTO create(BudgetDTO dto) {
        Budget budget = Budget.builder()
                .anneeScolaire(dto.getAnneeScolaire())
                .label(dto.getLabel())
                .type(dto.getType())
                .categorie(dto.getCategorie())
                .montantPrevu(dto.getMontantPrevu() != null ? dto.getMontantPrevu() : BigDecimal.ZERO)
                .montantRealise(dto.getMontantRealise() != null ? dto.getMontantRealise() : BigDecimal.ZERO)
                .mois(dto.getMois())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toDTO(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetDTO update(UUID id, BudgetDTO dto) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));

        budget.setAnneeScolaire(dto.getAnneeScolaire());
        budget.setLabel(dto.getLabel());
        budget.setType(dto.getType());
        budget.setCategorie(dto.getCategorie());
        budget.setMontantPrevu(dto.getMontantPrevu() != null ? dto.getMontantPrevu() : BigDecimal.ZERO);
        budget.setMontantRealise(dto.getMontantRealise() != null ? dto.getMontantRealise() : BigDecimal.ZERO);
        budget.setMois(dto.getMois());

        return toDTO(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(UUID id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget", id);
        }
        budgetRepository.deleteById(id);
    }

    public PrevisionDTO getPrevisions(String anneeScolaire) {
        List<Budget> budgets = budgetRepository.findByAnneeScolaire(anneeScolaire);

        BigDecimal totalRecettesPrevues = BigDecimal.ZERO;
        BigDecimal totalRecettesRealisees = BigDecimal.ZERO;
        BigDecimal totalDepensesPrevues = BigDecimal.ZERO;
        BigDecimal totalDepensesRealisees = BigDecimal.ZERO;

        for (Budget b : budgets) {
            if ("RECETTE".equals(b.getType())) {
                totalRecettesPrevues = totalRecettesPrevues.add(
                        b.getMontantPrevu() != null ? b.getMontantPrevu() : BigDecimal.ZERO);
                totalRecettesRealisees = totalRecettesRealisees.add(
                        b.getMontantRealise() != null ? b.getMontantRealise() : BigDecimal.ZERO);
            } else if ("DEPENSE".equals(b.getType())) {
                totalDepensesPrevues = totalDepensesPrevues.add(
                        b.getMontantPrevu() != null ? b.getMontantPrevu() : BigDecimal.ZERO);
                totalDepensesRealisees = totalDepensesRealisees.add(
                        b.getMontantRealise() != null ? b.getMontantRealise() : BigDecimal.ZERO);
            }
        }

        BigDecimal soldePrevu = totalRecettesPrevues.subtract(totalDepensesPrevues);
        BigDecimal soldeRealise = totalRecettesRealisees.subtract(totalDepensesRealisees);
        BigDecimal variance = soldeRealise.subtract(soldePrevu);

        List<BudgetDTO> budgetDTOs = budgets.stream().map(this::toDTO).toList();

        return PrevisionDTO.builder()
                .anneeScolaire(anneeScolaire)
                .totalRecettesPrevues(totalRecettesPrevues)
                .totalRecettesRealisees(totalRecettesRealisees)
                .totalDepensesPrevues(totalDepensesPrevues)
                .totalDepensesRealisees(totalDepensesRealisees)
                .soldePrevu(soldePrevu)
                .soldeRealise(soldeRealise)
                .variance(variance)
                .budgets(budgetDTOs)
                .build();
    }

    private BudgetDTO toDTO(Budget budget) {
        BigDecimal prevu = budget.getMontantPrevu() != null ? budget.getMontantPrevu() : BigDecimal.ZERO;
        BigDecimal realise = budget.getMontantRealise() != null ? budget.getMontantRealise() : BigDecimal.ZERO;

        return BudgetDTO.builder()
                .id(budget.getId())
                .anneeScolaire(budget.getAnneeScolaire())
                .label(budget.getLabel())
                .type(budget.getType())
                .categorie(budget.getCategorie())
                .montantPrevu(prevu)
                .montantRealise(realise)
                .variance(realise.subtract(prevu))
                .mois(budget.getMois())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
