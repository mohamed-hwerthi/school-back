package com.schoolSys.schooolSys.devoir;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.devoir.dto.CreateDevoirRequest;
import com.schoolSys.schooolSys.devoir.dto.DevoirDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DevoirService {

    private final DevoirRepository devoirRepository;
    private final SoumissionRepository soumissionRepository;
    private final CurrentUserContext currentUser;
    private final AnneeScolaireProvider anneeScolaireProvider;

    public List<DevoirDTO> findAll(UUID classeId, UUID moduleId, String anneeScolaire) {
        String year = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        List<Devoir> devoirs;
        if (classeId != null && moduleId != null) {
            devoirs = devoirRepository.findByClasseIdAndModuleIdAndAnneeScolaireOrderByDateLimiteDesc(classeId, moduleId, year);
        } else if (classeId != null) {
            devoirs = devoirRepository.findByClasseIdAndAnneeScolaireOrderByDateLimiteDesc(classeId, year);
        } else if (moduleId != null) {
            devoirs = devoirRepository.findByModuleIdAndAnneeScolaireOrderByDateLimiteDesc(moduleId, year);
        } else {
            devoirs = devoirRepository.findByAnneeScolaireOrderByDateLimiteDesc(year);
        }
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            var scoped = currentUser.getScopedClasseIdsForTeacher();
            devoirs = devoirs.stream()
                    .filter(d -> d.getClasseId() == null || scoped.contains(d.getClasseId()))
                    .toList();
        }
        return devoirs.stream().map(this::toDTO).toList();
    }

    public DevoirDTO findById(UUID id) {
        Devoir devoir = devoirRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devoir", id));
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && devoir.getClasseId() != null
                && !currentUser.teacherTeachesClasse(devoir.getClasseId())) {
            throw new AccessDeniedException("Ce devoir n'est pas dans votre périmètre.");
        }
        return toDTO(devoir);
    }

    public List<DevoirDTO> findByClasse(UUID classeId) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && !currentUser.teacherTeachesClasse(classeId)) {
            throw new AccessDeniedException("Vous n'enseignez pas dans cette classe.");
        }
        return devoirRepository.findByClasseIdOrderByDateLimiteDesc(classeId)
                .stream().map(this::toDTO).toList();
    }

    public List<DevoirDTO> findByModule(UUID moduleId) {
        List<Devoir> devoirs = devoirRepository.findByModuleIdOrderByDateLimiteDesc(moduleId);
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            var scoped = currentUser.getScopedClasseIdsForTeacher();
            devoirs = devoirs.stream()
                    .filter(d -> d.getClasseId() == null || scoped.contains(d.getClasseId()))
                    .toList();
        }
        return devoirs.stream().map(this::toDTO).toList();
    }

    @Transactional
    public DevoirDTO create(CreateDevoirRequest request) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && request.getClasseId() != null
                && !currentUser.teacherTeachesClasse(request.getClasseId())) {
            throw new AccessDeniedException("Vous n'enseignez pas dans cette classe.");
        }
        Devoir devoir = Devoir.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .moduleId(request.getModuleId())
                .classeId(request.getClasseId())
                .enseignantId(request.getEnseignantId())
                .datePublication(request.getDatePublication() != null ? request.getDatePublication() : LocalDate.now())
                .dateLimite(request.getDateLimite())
                .type(request.getType())
                .pointsMax(request.getPointsMax())
                .fichierUrl(request.getFichierUrl())
                .statut(request.getStatut())
                .anneeScolaire(anneeScolaireProvider.resolveAnneeScolaire(request.getAnneeScolaire()))
                .build();
        return toDTO(devoirRepository.save(devoir));
    }

    @Transactional
    public DevoirDTO update(UUID id, CreateDevoirRequest request) {
        Devoir devoir = devoirRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devoir", id));

        devoir.setTitre(request.getTitre());
        devoir.setDescription(request.getDescription());
        devoir.setModuleId(request.getModuleId());
        devoir.setClasseId(request.getClasseId());
        devoir.setEnseignantId(request.getEnseignantId());
        if (request.getDatePublication() != null) {
            devoir.setDatePublication(request.getDatePublication());
        }
        devoir.setDateLimite(request.getDateLimite());
        devoir.setType(request.getType());
        devoir.setPointsMax(request.getPointsMax());
        devoir.setFichierUrl(request.getFichierUrl());
        devoir.setStatut(request.getStatut());
        if (request.getAnneeScolaire() != null) {
            devoir.setAnneeScolaire(request.getAnneeScolaire());
        }
        devoir.setUpdatedAt(LocalDateTime.now());

        return toDTO(devoirRepository.save(devoir));
    }

    @Transactional
    public void delete(UUID id) {
        if (!devoirRepository.existsById(id)) {
            throw new ResourceNotFoundException("Devoir", id);
        }
        devoirRepository.deleteById(id);
    }

    @Transactional
    public DevoirDTO closeDevoir(UUID id) {
        Devoir devoir = devoirRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devoir", id));
        devoir.setStatut("FERME");
        devoir.setUpdatedAt(LocalDateTime.now());
        return toDTO(devoirRepository.save(devoir));
    }

    private DevoirDTO toDTO(Devoir devoir) {
        long totalSoumissions = soumissionRepository.countByDevoirId(devoir.getId());
        return DevoirDTO.builder()
                .id(devoir.getId())
                .titre(devoir.getTitre())
                .description(devoir.getDescription())
                .moduleId(devoir.getModuleId())
                .classeId(devoir.getClasseId())
                .enseignantId(devoir.getEnseignantId())
                .datePublication(devoir.getDatePublication())
                .dateLimite(devoir.getDateLimite())
                .type(devoir.getType())
                .pointsMax(devoir.getPointsMax())
                .fichierUrl(devoir.getFichierUrl())
                .statut(devoir.getStatut())
                .anneeScolaire(devoir.getAnneeScolaire())
                .totalSoumissions(totalSoumissions)
                .createdAt(devoir.getCreatedAt())
                .updatedAt(devoir.getUpdatedAt())
                .build();
    }
}
