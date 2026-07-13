package com.schoolSys.schooolSys.personnel;

import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.personnel.dto.PersonnelRequestDTO;
import com.schoolSys.schooolSys.personnel.dto.PersonnelResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PersonnelService {

    private final PersonnelRepository personnelRepository;
    private final PersonnelMapper personnelMapper;

    public List<PersonnelResponseDTO> findAll() {
        return personnelRepository.findAll().stream()
                .map(personnelMapper::toResponseDTO)
                .toList();
    }

    public PagedResponse<PersonnelResponseDTO> findPage(String search, String fonction, String statut, int page, int size) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        String normalizedFonction = (fonction == null || fonction.isBlank()) ? null : fonction.trim();
        String normalizedStatut = (statut == null || statut.isBlank()) ? null : statut.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        Page<Personnel> result = personnelRepository.findFiltered(normalizedSearch, normalizedFonction, normalizedStatut, pageable);
        List<PersonnelResponseDTO> dtos = result.getContent().stream()
                .map(personnelMapper::toResponseDTO)
                .toList();
        return PagedResponse.from(result, dtos);
    }

    public PersonnelResponseDTO findById(UUID id) {
        Personnel personnel = personnelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personnel", id));
        return personnelMapper.toResponseDTO(personnel);
    }

    @Transactional
    public PersonnelResponseDTO create(PersonnelRequestDTO dto) {
        Personnel personnel = personnelMapper.toEntity(dto);
        Personnel saved = personnelRepository.save(personnel);
        return personnelMapper.toResponseDTO(saved);
    }

    @Transactional
    public PersonnelResponseDTO update(UUID id, PersonnelRequestDTO dto) {
        Personnel personnel = personnelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personnel", id));
        personnelMapper.updateEntity(dto, personnel);
        return personnelMapper.toResponseDTO(personnelRepository.save(personnel));
    }

    @Transactional
    public void delete(UUID id) {
        if (!personnelRepository.existsById(id)) {
            throw new ResourceNotFoundException("Personnel", id);
        }
        personnelRepository.deleteById(id);
    }

    @Transactional
    public List<PersonnelResponseDTO> importBulk(List<PersonnelRequestDTO> dtos) {
        List<Personnel> personnels = dtos.stream()
                .map(personnelMapper::toEntity)
                .toList();
        return personnelRepository.saveAll(personnels).stream()
                .map(personnelMapper::toResponseDTO)
                .toList();
    }
}
