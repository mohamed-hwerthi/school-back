package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.rh.dto.CreatePointageRequest;
import com.schoolSys.schooolSys.rh.dto.PointageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointagePersonnelService {

    private final PointagePersonnelRepository pointageRepository;

    public List<PointageDTO> getAll() {
        return pointageRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PointageDTO getById(UUID id) {
        PointagePersonnel p = pointageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointagePersonnel", id));
        return toDto(p);
    }

    public List<PointageDTO> getByDate(LocalDate date) {
        return pointageRepository.findByDatePointage(date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PointageDTO> getByEmploye(UUID employeId, String employeType) {
        return pointageRepository.findByEmployeIdAndEmployeType(employeId, employeType).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PointageDTO create(CreatePointageRequest dto) {
        PointagePersonnel pointage = PointagePersonnel.builder()
                .employeId(dto.getEmployeId())
                .employeType(dto.getEmployeType())
                .datePointage(dto.getDatePointage() != null ? dto.getDatePointage() : LocalDate.now())
                .heureArrivee(dto.getHeureArrivee())
                .heureDepart(dto.getHeureDepart())
                .heuresTravaillees(dto.getHeuresTravaillees())
                .statut(dto.getStatut() != null ? dto.getStatut() : "PRESENT")
                .notes(dto.getNotes())
                .build();

        return toDto(pointageRepository.save(pointage));
    }

    @Transactional
    public PointageDTO update(UUID id, CreatePointageRequest dto) {
        PointagePersonnel pointage = pointageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PointagePersonnel", id));

        pointage.setEmployeId(dto.getEmployeId());
        pointage.setEmployeType(dto.getEmployeType());
        if (dto.getDatePointage() != null) {
            pointage.setDatePointage(dto.getDatePointage());
        }
        pointage.setHeureArrivee(dto.getHeureArrivee());
        pointage.setHeureDepart(dto.getHeureDepart());
        pointage.setHeuresTravaillees(dto.getHeuresTravaillees());
        if (dto.getStatut() != null) {
            pointage.setStatut(dto.getStatut());
        }
        pointage.setNotes(dto.getNotes());

        return toDto(pointageRepository.save(pointage));
    }

    @Transactional
    public void delete(UUID id) {
        if (!pointageRepository.existsById(id)) {
            throw new ResourceNotFoundException("PointagePersonnel", id);
        }
        pointageRepository.deleteById(id);
    }

    private PointageDTO toDto(PointagePersonnel p) {
        return PointageDTO.builder()
                .id(p.getId())
                .employeId(p.getEmployeId())
                .employeType(p.getEmployeType())
                .datePointage(p.getDatePointage())
                .heureArrivee(p.getHeureArrivee())
                .heureDepart(p.getHeureDepart())
                .heuresTravaillees(p.getHeuresTravaillees())
                .statut(p.getStatut())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
