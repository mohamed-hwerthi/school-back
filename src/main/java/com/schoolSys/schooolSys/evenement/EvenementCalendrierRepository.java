package com.schoolSys.schooolSys.evenement;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EvenementCalendrierRepository extends JpaRepository<EvenementCalendrier, UUID> {

    List<EvenementCalendrier> findAllByOrderByDateDebutAsc();

    List<EvenementCalendrier> findByDateDebutBetweenOrderByDateDebutAsc(LocalDate from, LocalDate to);

    List<EvenementCalendrier> findByTypeOrderByDateDebutAsc(String type);
}
