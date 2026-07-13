package com.schoolSys.schooolSys.appreciation;

import java.util.UUID;

import com.schoolSys.schooolSys.appreciation.dto.*;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.domaine.Domaine;
import com.schoolSys.schooolSys.domaine.DomaineRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppreciationService {

    private final RecommandationRepository recommandationRepository;
    private final ObservationRepository observationRepository;
    private final StudentRepository studentRepository;
    private final DomaineRepository domaineRepository;
    private final CurrentUserContext currentUser;

    /** Keeps only the student IDs the current user is allowed to read. */
    private List<UUID> scopeStudentIds(List<UUID> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) return List.of();
        return studentIds.stream().filter(id -> {
            try {
                currentUser.assertCanAccessStudent(id);
                return true;
            } catch (AccessDeniedException e) {
                return false;
            }
        }).toList();
    }

    // ── Recommandations ─────────────────────────────────────

    public List<RecommandationDTO> getRecommandations(List<UUID> studentIds, Integer trimestre) {
        List<UUID> scoped = scopeStudentIds(studentIds);
        if (scoped.isEmpty()) return List.of();
        return recommandationRepository.findByStudentIdInAndTrimestre(scoped, trimestre)
                .stream().map(this::toRecommandationDTO).toList();
    }

    @Transactional
    public List<RecommandationDTO> upsertRecommandations(List<RecommandationRequestDTO> dtos) {
        dtos.forEach(d -> currentUser.assertCanAccessStudent(d.getStudentId()));
        // Row-level scoping (en plus de l'accès élève) : un ENSEIGNANT ne peut
        // écrire que des recommandations sur les domaines où il enseigne au
        // moins une matière. ADMIN / DIRECTEUR peuvent écrire partout.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scopedDomaines = currentUser.getScopedDomaineIdsForTeacher();
            for (RecommandationRequestDTO d : dtos) {
                if (!scopedDomaines.contains(d.getDomaineId())) {
                    throw new AccessDeniedException(
                            "Vous n'enseignez pas dans ce domaine.");
                }
            }
        }
        List<Recommandation> saved = new ArrayList<>();

        for (RecommandationRequestDTO dto : dtos) {
            Optional<Recommandation> existing = recommandationRepository
                    .findByStudentIdAndDomaineIdAndTrimestre(
                            dto.getStudentId(), dto.getDomaineId(), dto.getTrimestre());

            if (existing.isPresent()) {
                Recommandation r = existing.get();
                r.setTexte(dto.getTexte());
                r.setUpdatedAt(LocalDateTime.now());
                saved.add(recommandationRepository.save(r));
            } else {
                Student student = studentRepository.findById(dto.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
                Domaine domaine = domaineRepository.findById(dto.getDomaineId())
                        .orElseThrow(() -> new ResourceNotFoundException("Domaine", dto.getDomaineId()));

                Recommandation r = Recommandation.builder()
                        .student(student)
                        .domaine(domaine)
                        .trimestre(dto.getTrimestre())
                        .texte(dto.getTexte())
                        .build();
                saved.add(recommandationRepository.save(r));
            }
        }

        return saved.stream().map(this::toRecommandationDTO).toList();
    }

    // ── Observations ────────────────────────────────────────

    public List<ObservationDTO> getObservations(List<UUID> studentIds, Integer trimestre) {
        List<UUID> scoped = scopeStudentIds(studentIds);
        if (scoped.isEmpty()) return List.of();
        return observationRepository.findByStudentIdInAndTrimestre(scoped, trimestre)
                .stream().map(this::toObservationDTO).toList();
    }

    @Transactional
    public List<ObservationDTO> upsertObservations(List<ObservationRequestDTO> dtos) {
        dtos.forEach(d -> currentUser.assertCanAccessStudent(d.getStudentId()));
        List<ObservationTrimestre> saved = new ArrayList<>();

        for (ObservationRequestDTO dto : dtos) {
            Optional<ObservationTrimestre> existing = observationRepository
                    .findByStudentIdAndTrimestre(dto.getStudentId(), dto.getTrimestre());

            if (existing.isPresent()) {
                ObservationTrimestre o = existing.get();
                o.setComportement(dto.getComportement());
                o.setCertificatType(dto.getCertificatType());
                o.setUpdatedAt(LocalDateTime.now());
                saved.add(observationRepository.save(o));
            } else {
                Student student = studentRepository.findById(dto.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));

                ObservationTrimestre o = ObservationTrimestre.builder()
                        .student(student)
                        .trimestre(dto.getTrimestre())
                        .comportement(dto.getComportement())
                        .certificatType(dto.getCertificatType())
                        .build();
                saved.add(observationRepository.save(o));
            }
        }

        return saved.stream().map(this::toObservationDTO).toList();
    }

    // ── Mapping ─────────────────────────────────────────────

    private RecommandationDTO toRecommandationDTO(Recommandation r) {
        return RecommandationDTO.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getFirstName() + " " + r.getStudent().getLastName())
                .domaineId(r.getDomaine().getId())
                .domaineName(r.getDomaine().getName())
                .trimestre(r.getTrimestre())
                .texte(r.getTexte())
                .build();
    }

    private ObservationDTO toObservationDTO(ObservationTrimestre o) {
        return ObservationDTO.builder()
                .id(o.getId())
                .studentId(o.getStudent().getId())
                .studentName(o.getStudent().getFirstName() + " " + o.getStudent().getLastName())
                .trimestre(o.getTrimestre())
                .comportement(o.getComportement())
                .certificatType(o.getCertificatType())
                .build();
    }
}
