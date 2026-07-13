package com.schoolSys.schooolSys.appelparent;

import java.util.UUID;

import com.schoolSys.schooolSys.appelparent.dto.AppelParentRequestDTO;
import com.schoolSys.schooolSys.appelparent.dto.AppelParentResponseDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppelParentService {

    private final AppelParentRepository repository;
    private final AppelParentMapper mapper;

    public List<AppelParentResponseDTO> findAll(UUID eleveId) {
        List<AppelParent> rows = eleveId != null
                ? repository.findByEleveIdOrderByDateAppelDesc(eleveId)
                : repository.findAllByOrderByDateAppelDesc();
        return mapper.toResponseDTOList(rows);
    }

    public AppelParentResponseDTO findById(UUID id) {
        return mapper.toResponseDTO(load(id));
    }

    @Transactional
    public AppelParentResponseDTO create(AppelParentRequestDTO dto) {
        AppelParent entity = mapper.toEntity(dto);
        if (entity.getDateAppel() == null) {
            entity.setDateAppel(LocalDateTime.now());
        }
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Transactional
    public AppelParentResponseDTO update(UUID id, AppelParentRequestDTO dto) {
        AppelParent entity = load(id);
        mapper.updateEntity(dto, entity);
        return mapper.toResponseDTO(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("AppelParent", id);
        }
        repository.deleteById(id);
    }

    private AppelParent load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppelParent", id));
    }
}
