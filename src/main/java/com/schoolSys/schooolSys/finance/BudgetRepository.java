package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByAnneeScolaire(String anneeScolaire);

    List<Budget> findByAnneeScolaireAndType(String anneeScolaire, String type);
}
