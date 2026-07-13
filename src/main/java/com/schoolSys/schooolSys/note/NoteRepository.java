package com.schoolSys.schooolSys.note;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findByExamenIdAndTrimestreAndAnneeScolaire(UUID examenId, Integer trimestre, String anneeScolaire);

    List<Note> findByStudentIdAndTrimestreAndAnneeScolaire(UUID studentId, Integer trimestre, String anneeScolaire);

    Optional<Note> findByStudentIdAndExamenIdAndTrimestreAndAnneeScolaire(UUID studentId, UUID examenId, Integer trimestre, String anneeScolaire);

    List<Note> findByExamenClasseIdAndTrimestreAndAnneeScolaire(UUID classeId, Integer trimestre, String anneeScolaire);

    long countByExamenIdAndTrimestreAndAnneeScolaire(UUID examenId, Integer trimestre, String anneeScolaire);

    // Legacy queries kept for backward compatibility (will use context fallback)
    @Deprecated
    List<Note> findByExamenIdAndTrimestre(UUID examenId, Integer trimestre);

    @Deprecated
    List<Note> findByStudentIdAndTrimestre(UUID studentId, Integer trimestre);

    @Deprecated
    Optional<Note> findByStudentIdAndExamenIdAndTrimestre(UUID studentId, UUID examenId, Integer trimestre);

    @Deprecated
    List<Note> findByExamenClasseIdAndTrimestre(UUID classeId, Integer trimestre);

    @Deprecated
    long countByExamenIdAndTrimestre(UUID examenId, Integer trimestre);
}
