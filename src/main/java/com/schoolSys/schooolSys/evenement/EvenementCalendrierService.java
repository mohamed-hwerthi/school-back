package com.schoolSys.schooolSys.evenement;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.evenement.dto.EvenementCalendrierRequestDTO;
import com.schoolSys.schooolSys.evenement.dto.EvenementCalendrierResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvenementCalendrierService {

    private final EvenementCalendrierRepository repository;
    private final EvenementCalendrierMapper mapper;

    public List<EvenementCalendrierResponseDTO> findAll(LocalDate from, LocalDate to, String type) {
        List<EvenementCalendrier> events;
        if (from != null && to != null) {
            events = repository.findByDateDebutBetweenOrderByDateDebutAsc(from, to);
        } else if (type != null && !type.isBlank()) {
            events = repository.findByTypeOrderByDateDebutAsc(type);
        } else {
            events = repository.findAllByOrderByDateDebutAsc();
        }
        return mapper.toResponseDTOList(events);
    }

    public EvenementCalendrierResponseDTO findById(UUID id) {
        EvenementCalendrier ev = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvenementCalendrier", id));
        return mapper.toResponseDTO(ev);
    }

    @Transactional
    public EvenementCalendrierResponseDTO create(EvenementCalendrierRequestDTO dto) {
        EvenementCalendrier ev = mapper.toEntity(dto);
        ev.setCreatedAt(LocalDateTime.now());
        ev.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponseDTO(repository.save(ev));
    }

    @Transactional
    public EvenementCalendrierResponseDTO update(UUID id, EvenementCalendrierRequestDTO dto) {
        EvenementCalendrier ev = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvenementCalendrier", id));
        mapper.updateEntity(dto, ev);
        ev.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponseDTO(repository.save(ev));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("EvenementCalendrier", id);
        }
        repository.deleteById(id);
    }
}
