package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.rh.dto.*;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormationService {

    private final FormationRepository formationRepository;
    private final FormationParticipantRepository participantRepository;
    private final TeacherRepository teacherRepository;
    private final FichePaieRepository fichePaieRepository;
    private final PointagePersonnelRepository pointageRepository;

    public List<FormationDTO> getAll() {
        return formationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FormationDTO getById(UUID id) {
        Formation f = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation", id));
        return toDto(f);
    }

    @Transactional
    public FormationDTO create(CreateFormationRequest dto) {
        Formation formation = Formation.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .formateur(dto.getFormateur())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .lieu(dto.getLieu())
                .nombreHeures(dto.getNombreHeures())
                .cout(dto.getCout())
                .statut(dto.getStatut() != null ? dto.getStatut() : "PLANIFIEE")
                .build();

        return toDto(formationRepository.save(formation));
    }

    @Transactional
    public FormationDTO update(UUID id, CreateFormationRequest dto) {
        Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation", id));

        formation.setTitre(dto.getTitre());
        formation.setDescription(dto.getDescription());
        formation.setFormateur(dto.getFormateur());
        formation.setDateDebut(dto.getDateDebut());
        formation.setDateFin(dto.getDateFin());
        formation.setLieu(dto.getLieu());
        formation.setNombreHeures(dto.getNombreHeures());
        formation.setCout(dto.getCout());
        if (dto.getStatut() != null) {
            formation.setStatut(dto.getStatut());
        }

        return toDto(formationRepository.save(formation));
    }

    @Transactional
    public void delete(UUID id) {
        if (!formationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Formation", id);
        }
        formationRepository.deleteById(id);
    }

    @Transactional
    public FormationParticipantDTO addParticipant(UUID formationId, AddParticipantRequest dto) {
        Formation formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation", formationId));

        FormationParticipant participant = FormationParticipant.builder()
                .formation(formation)
                .employeId(dto.getEmployeId())
                .employeType(dto.getEmployeType())
                .present(dto.getPresent() != null ? dto.getPresent() : false)
                .certificatObtenu(dto.getCertificatObtenu() != null ? dto.getCertificatObtenu() : false)
                .build();

        return toParticipantDto(participantRepository.save(participant));
    }

    @Transactional
    public void removeParticipant(UUID participantId) {
        if (!participantRepository.existsById(participantId)) {
            throw new ResourceNotFoundException("FormationParticipant", participantId);
        }
        participantRepository.deleteById(participantId);
    }

    public RhStatsDTO getStats() {
        long totalTeachers = teacherRepository.count();

        LocalDate now = LocalDate.now();
        BigDecimal masseSalariale = fichePaieRepository.sumSalaireNetByMoisAndAnnee(
                now.getMonthValue(), now.getYear());

        long formationsEnCours = formationRepository.findByStatut("EN_COURS").size();

        long totalPointages = pointageRepository.findByDatePointage(now).size();
        long presents = pointageRepository.countByDatePointageAndStatut(now, "PRESENT");
        double tauxPresence = totalPointages > 0 ? (double) presents / totalPointages * 100 : 0;

        return RhStatsDTO.builder()
                .totalEmployes(totalTeachers)
                .masseSalariale(masseSalariale)
                .formationsEnCours(formationsEnCours)
                .tauxPresence(Math.round(tauxPresence * 100.0) / 100.0)
                .build();
    }

    private FormationDTO toDto(Formation f) {
        List<FormationParticipantDTO> participantDtos = f.getParticipants() != null
                ? f.getParticipants().stream().map(this::toParticipantDto).collect(Collectors.toList())
                : List.of();

        return FormationDTO.builder()
                .id(f.getId())
                .titre(f.getTitre())
                .description(f.getDescription())
                .formateur(f.getFormateur())
                .dateDebut(f.getDateDebut())
                .dateFin(f.getDateFin())
                .lieu(f.getLieu())
                .nombreHeures(f.getNombreHeures())
                .cout(f.getCout())
                .statut(f.getStatut())
                .participants(participantDtos)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    private FormationParticipantDTO toParticipantDto(FormationParticipant p) {
        return FormationParticipantDTO.builder()
                .id(p.getId())
                .formationId(p.getFormation().getId())
                .employeId(p.getEmployeId())
                .employeType(p.getEmployeType())
                .present(p.getPresent())
                .certificatObtenu(p.getCertificatObtenu())
                .build();
    }
}
