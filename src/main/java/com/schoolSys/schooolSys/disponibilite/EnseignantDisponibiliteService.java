package com.schoolSys.schooolSys.disponibilite;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.disponibilite.dto.EnseignantDisponibiliteRequestDTO;
import com.schoolSys.schooolSys.disponibilite.dto.EnseignantDisponibiliteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnseignantDisponibiliteService {

    private final EnseignantDisponibiliteRepository repository;
    private final EnseignantDisponibiliteMapper mapper;

    public List<EnseignantDisponibiliteResponseDTO> findAll() {
        return mapper.toResponseDTOList(repository.findAll());
    }

    public List<EnseignantDisponibiliteResponseDTO> findByEnseignant(UUID enseignantId) {
        return mapper.toResponseDTOList(repository.findByEnseignantId(enseignantId));
    }

    public EnseignantDisponibiliteResponseDTO findById(UUID id) {
        return mapper.toResponseDTO(load(id));
    }

    @Transactional
    public EnseignantDisponibiliteResponseDTO create(EnseignantDisponibiliteRequestDTO dto) {
        EnseignantDisponibilite entity = mapper.toEntity(dto);
        try {
            return mapper.toResponseDTO(repository.save(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                "Une disponibilité existe déjà pour cet enseignant sur ce créneau", ex);
        }
    }

    @Transactional
    public EnseignantDisponibiliteResponseDTO update(UUID id, EnseignantDisponibiliteRequestDTO dto) {
        EnseignantDisponibilite entity = load(id);
        mapper.updateEntity(dto, entity);
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("EnseignantDisponibilite", id);
        }
        repository.deleteById(id);
    }

    private EnseignantDisponibilite load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EnseignantDisponibilite", id));
    }
}
