package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.emploidutemps.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmploiDuTempsService {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final CreneauRepository creneauRepository;
    private final RemplacementRepository remplacementRepository;

    public List<EmploiDuTempsResponseDTO> getByClasse(UUID classeId) {
        return emploiDuTempsRepository.findByClasseId(classeId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<EmploiDuTempsResponseDTO> getByEnseignant(UUID enseignantId) {
        return emploiDuTempsRepository.findByEnseignantId(enseignantId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public List<EmploiDuTempsResponseDTO> saveAll(UUID classeId, List<EmploiDuTempsRequestDTO> requests) {
        emploiDuTempsRepository.deleteByClasseId(classeId);
        List<EmploiDuTemps> entries = requests.stream()
            .map(req -> EmploiDuTemps.builder()
                .classeId(classeId)
                .creneauId(req.getCreneauId())
                .jourSemaine(req.getJourSemaine())
                .moduleId(req.getModuleId())
                .enseignantId(req.getEnseignantId())
                .salle(req.getSalle())
                .build())
            .collect(Collectors.toList());
        return emploiDuTempsRepository.saveAll(entries).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public List<ConflitDTO> detectConflits(List<EmploiDuTempsRequestDTO> requests) {
        List<ConflitDTO> conflits = new ArrayList<>();
        for (EmploiDuTempsRequestDTO req : requests) {
            if (req.getEnseignantId() != null) {
                List<EmploiDuTemps> teacherConflicts = emploiDuTempsRepository
                    .findByEnseignantIdAndJourSemaineAndCreneauId(
                        req.getEnseignantId(), req.getJourSemaine(), req.getCreneauId());
                if (!teacherConflicts.isEmpty()) {
                    conflits.add(ConflitDTO.builder()
                        .typeConflit("ENSEIGNANT")
                        .jourSemaine(req.getJourSemaine())
                        .creneauId(req.getCreneauId())
                        .enseignantId(req.getEnseignantId())
                        .message("Enseignant déjà affecté sur ce créneau")
                        .build());
                }
            }
            if (req.getSalle() != null && !req.getSalle().isBlank()) {
                List<EmploiDuTemps> roomConflicts = emploiDuTempsRepository
                    .findBySalleAndJourSemaineAndCreneauId(
                        req.getSalle(), req.getJourSemaine(), req.getCreneauId());
                if (!roomConflicts.isEmpty()) {
                    conflits.add(ConflitDTO.builder()
                        .typeConflit("SALLE")
                        .jourSemaine(req.getJourSemaine())
                        .creneauId(req.getCreneauId())
                        .salle(req.getSalle())
                        .message("Salle déjà occupée sur ce créneau")
                        .build());
                }
            }
        }
        return conflits;
    }

    // --- Creneau CRUD ---

    public List<CreneauDTO> getAllCreneaux() {
        return creneauRepository.findAll().stream()
            .map(this::toCreneauDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public CreneauDTO createCreneau(CreneauDTO dto) {
        Creneau creneau = Creneau.builder()
            .label(dto.getLabel())
            .heureDebut(dto.getHeureDebut())
            .heureFin(dto.getHeureFin())
            .type(dto.getType())
            .build();
        return toCreneauDto(creneauRepository.save(creneau));
    }

    @Transactional
    public void deleteCreneau(UUID id) {
        if (!creneauRepository.existsById(id)) throw new ResourceNotFoundException("Creneau", id);
        creneauRepository.deleteById(id);
    }

    // --- Remplacements ---

    @Transactional
    public RemplacementResponseDTO createRemplacement(RemplacementRequestDTO dto) {
        Remplacement r = Remplacement.builder()
            .emploiDuTempsId(dto.getEmploiDuTempsId())
            .enseignantRemplacantId(dto.getEnseignantRemplacantId())
            .dateDebut(dto.getDateDebut())
            .dateFin(dto.getDateFin())
            .motif(dto.getMotif())
            .build();
        return toRemplacementDto(remplacementRepository.save(r));
    }

    public List<RemplacementResponseDTO> getRemplacements(UUID enseignantId, LocalDate from, LocalDate to) {
        return remplacementRepository
            .findByEnseignantRemplacantIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(enseignantId, to, from)
            .stream().map(this::toRemplacementDto).collect(Collectors.toList());
    }

    public List<RemplacementResponseDTO> getAllRemplacements() {
        return remplacementRepository.findAll().stream()
            .map(this::toRemplacementDto).collect(Collectors.toList());
    }

    @Transactional
    public void deleteRemplacement(UUID id) {
        if (!remplacementRepository.existsById(id)) throw new ResourceNotFoundException("Remplacement", id);
        remplacementRepository.deleteById(id);
    }

    // --- Mappers ---

    private EmploiDuTempsResponseDTO toDto(EmploiDuTemps e) {
        return EmploiDuTempsResponseDTO.builder()
            .id(e.getId())
            .classeId(e.getClasseId())
            .creneauId(e.getCreneauId())
            .jourSemaine(e.getJourSemaine())
            .moduleId(e.getModuleId())
            .enseignantId(e.getEnseignantId())
            .salle(e.getSalle())
            .classroomId(e.getClassroomId())
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .build();
    }

    private CreneauDTO toCreneauDto(Creneau c) {
        return CreneauDTO.builder()
            .id(c.getId())
            .label(c.getLabel())
            .heureDebut(c.getHeureDebut())
            .heureFin(c.getHeureFin())
            .type(c.getType())
            .build();
    }

    private RemplacementResponseDTO toRemplacementDto(Remplacement r) {
        return RemplacementResponseDTO.builder()
            .id(r.getId())
            .emploiDuTempsId(r.getEmploiDuTempsId())
            .enseignantRemplacantId(r.getEnseignantRemplacantId())
            .dateDebut(r.getDateDebut())
            .dateFin(r.getDateFin())
            .motif(r.getMotif())
            .createdAt(r.getCreatedAt())
            .build();
    }
}
