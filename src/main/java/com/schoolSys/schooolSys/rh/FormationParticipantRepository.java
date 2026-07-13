package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormationParticipantRepository extends JpaRepository<FormationParticipant, UUID> {

    List<FormationParticipant> findByFormationId(UUID formationId);

    List<FormationParticipant> findByEmployeId(UUID employeId);

    boolean existsByFormationIdAndEmployeId(UUID formationId, UUID employeId);
}
