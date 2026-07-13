package com.schoolSys.schooolSys.discipline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    List<Incident> findByDateBetween(LocalDate start, LocalDate end);

    List<Incident> findByType(String type);

    List<Incident> findByGravite(String gravite);

    @Query("SELECT i FROM Incident i JOIN i.elevesImpliques ie WHERE ie.eleveId = :eleveId ORDER BY i.createdAt DESC")
    List<Incident> findByEleveId(@Param("eleveId") UUID eleveId);
}
