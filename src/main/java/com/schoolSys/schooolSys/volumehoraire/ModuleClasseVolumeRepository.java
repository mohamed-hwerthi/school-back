package com.schoolSys.schooolSys.volumehoraire;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleClasseVolumeRepository
        extends JpaRepository<ModuleClasseVolume, UUID> {

    List<ModuleClasseVolume> findByClasseId(UUID classeId);

    List<ModuleClasseVolume> findByAnneeScolaireId(UUID anneeScolaireId);

    List<ModuleClasseVolume> findByClasseIdAndAnneeScolaireId(UUID classeId, UUID anneeScolaireId);

    Optional<ModuleClasseVolume> findByModuleIdAndClasseIdAndAnneeScolaireId(
            UUID moduleId, UUID classeId, UUID anneeScolaireId);
}
