package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.rh.dto.*;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RhService {

    private final ContratEnseignantRepository contratRepository;
    private final CongeRepository congeRepository;
    private final TeacherRepository teacherRepository;

    // ── Contrats ──────────────────────────────────────────────

    public List<ContratResponseDTO> getAllContrats() {
        return contratRepository.findAll().stream()
                .map(this::toContratDto)
                .collect(Collectors.toList());
    }

    public ContratResponseDTO getContratById(UUID id) {
        ContratEnseignant contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContratEnseignant", id));
        return toContratDto(contrat);
    }

    public List<ContratResponseDTO> getContratsByEnseignant(UUID enseignantId) {
        return contratRepository.findByEnseignantId(enseignantId).stream()
                .map(this::toContratDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContratResponseDTO createContrat(ContratRequestDTO dto) {
        Teacher enseignant = teacherRepository.findById(dto.getEnseignantId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", dto.getEnseignantId()));

        ContratEnseignant contrat = ContratEnseignant.builder()
                .enseignant(enseignant)
                .typeContrat(dto.getTypeContrat())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .salaire(dto.getSalaire())
                .statut(dto.getStatut() != null ? dto.getStatut() : "ACTIF")
                .observations(dto.getObservations())
                .build();

        return toContratDto(contratRepository.save(contrat));
    }

    @Transactional
    public ContratResponseDTO updateContrat(UUID id, ContratRequestDTO dto) {
        ContratEnseignant contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContratEnseignant", id));

        if (dto.getEnseignantId() != null) {
            Teacher enseignant = teacherRepository.findById(dto.getEnseignantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", dto.getEnseignantId()));
            contrat.setEnseignant(enseignant);
        }

        contrat.setTypeContrat(dto.getTypeContrat());
        contrat.setDateDebut(dto.getDateDebut());
        contrat.setDateFin(dto.getDateFin());
        contrat.setSalaire(dto.getSalaire());
        if (dto.getStatut() != null) {
            contrat.setStatut(dto.getStatut());
        }
        contrat.setObservations(dto.getObservations());

        return toContratDto(contratRepository.save(contrat));
    }

    @Transactional
    public void deleteContrat(UUID id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("ContratEnseignant", id);
        }
        contratRepository.deleteById(id);
    }

    // ── Conges ────────────────────────────────────────────────

    public List<CongeResponseDTO> getAllConges() {
        return congeRepository.findAll().stream()
                .map(this::toCongeDto)
                .collect(Collectors.toList());
    }

    public CongeResponseDTO getCongeById(UUID id) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", id));
        return toCongeDto(conge);
    }

    public List<CongeResponseDTO> getCongesByEnseignant(UUID enseignantId) {
        return congeRepository.findByEnseignantId(enseignantId).stream()
                .map(this::toCongeDto)
                .collect(Collectors.toList());
    }

    public List<CongeResponseDTO> getCongesByStatut(String statut) {
        return congeRepository.findByStatut(statut).stream()
                .map(this::toCongeDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CongeResponseDTO createConge(CongeRequestDTO dto) {
        Teacher enseignant = teacherRepository.findById(dto.getEnseignantId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", dto.getEnseignantId()));

        Conge conge = Conge.builder()
                .enseignant(enseignant)
                .typeConge(dto.getTypeConge())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .motif(dto.getMotif())
                .statut(dto.getStatut() != null ? dto.getStatut() : "EN_ATTENTE")
                .build();

        return toCongeDto(congeRepository.save(conge));
    }

    @Transactional
    public CongeResponseDTO updateConge(UUID id, CongeRequestDTO dto) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", id));

        if (dto.getEnseignantId() != null) {
            Teacher enseignant = teacherRepository.findById(dto.getEnseignantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", dto.getEnseignantId()));
            conge.setEnseignant(enseignant);
        }

        conge.setTypeConge(dto.getTypeConge());
        conge.setDateDebut(dto.getDateDebut());
        conge.setDateFin(dto.getDateFin());
        conge.setMotif(dto.getMotif());
        if (dto.getStatut() != null) {
            conge.setStatut(dto.getStatut());
        }

        return toCongeDto(congeRepository.save(conge));
    }

    @Transactional
    public CongeResponseDTO approuverConge(UUID id) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", id));
        conge.setStatut("APPROUVE");
        return toCongeDto(congeRepository.save(conge));
    }

    @Transactional
    public CongeResponseDTO refuserConge(UUID id) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", id));
        conge.setStatut("REFUSE");
        return toCongeDto(congeRepository.save(conge));
    }

    @Transactional
    public void deleteConge(UUID id) {
        if (!congeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Conge", id);
        }
        congeRepository.deleteById(id);
    }

    // ── Mappers ───────────────────────────────────────────────

    private ContratResponseDTO toContratDto(ContratEnseignant c) {
        Teacher t = c.getEnseignant();
        return ContratResponseDTO.builder()
                .id(c.getId())
                .enseignantId(t.getId())
                .enseignantNom(t.getLastName() + " " + t.getFirstName())
                .typeContrat(c.getTypeContrat())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .salaire(c.getSalaire())
                .statut(c.getStatut())
                .observations(c.getObservations())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private CongeResponseDTO toCongeDto(Conge c) {
        Teacher t = c.getEnseignant();
        return CongeResponseDTO.builder()
                .id(c.getId())
                .enseignantId(t.getId())
                .enseignantNom(t.getLastName() + " " + t.getFirstName())
                .typeConge(c.getTypeConge())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .motif(c.getMotif())
                .statut(c.getStatut())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
