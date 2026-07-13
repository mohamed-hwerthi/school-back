package com.schoolSys.schooolSys.volumehoraire;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.volumehoraire.dto.ModuleClasseVolumeRequestDTO;
import com.schoolSys.schooolSys.volumehoraire.dto.ModuleClasseVolumeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuleClasseVolumeService {

    private final ModuleClasseVolumeRepository repository;
    private final ModuleClasseVolumeMapper mapper;

    public List<ModuleClasseVolumeResponseDTO> findAll(UUID classeId, UUID anneeScolaireId) {
        List<ModuleClasseVolume> rows;
        if (classeId != null && anneeScolaireId != null) {
            rows = repository.findByClasseIdAndAnneeScolaireId(classeId, anneeScolaireId);
        } else if (classeId != null) {
            rows = repository.findByClasseId(classeId);
        } else if (anneeScolaireId != null) {
            rows = repository.findByAnneeScolaireId(anneeScolaireId);
        } else {
            rows = repository.findAll();
        }
        return mapper.toResponseDTOList(rows);
    }

    public ModuleClasseVolumeResponseDTO findById(UUID id) {
        return mapper.toResponseDTO(load(id));
    }

    @Transactional
    public ModuleClasseVolumeResponseDTO create(ModuleClasseVolumeRequestDTO dto) {
        ModuleClasseVolume entity = mapper.toEntity(dto);
        try {
            return mapper.toResponseDTO(repository.save(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                "Un volume horaire existe déjà pour ce module dans cette classe pour cette année", ex);
        }
    }

    @Transactional
    public ModuleClasseVolumeResponseDTO update(UUID id, ModuleClasseVolumeRequestDTO dto) {
        ModuleClasseVolume entity = load(id);
        mapper.updateEntity(dto, entity);
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("ModuleClasseVolume", id);
        }
        repository.deleteById(id);
    }

    private ModuleClasseVolume load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ModuleClasseVolume", id));
    }
}
