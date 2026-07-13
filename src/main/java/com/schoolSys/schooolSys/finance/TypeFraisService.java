package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.TypeFraisRequestDTO;
import com.schoolSys.schooolSys.finance.dto.TypeFraisResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypeFraisService {

    private final TypeFraisRepository typeFraisRepository;
    private final TypeFraisMapper typeFraisMapper;

    public List<TypeFraisResponseDTO> findAll() {
        return typeFraisRepository.findAll().stream()
                .map(typeFraisMapper::toResponseDTO)
                .toList();
    }

    public List<TypeFraisResponseDTO> findAllActifs() {
        return typeFraisRepository.findByActifTrue().stream()
                .map(typeFraisMapper::toResponseDTO)
                .toList();
    }

    public TypeFraisResponseDTO findById(UUID id) {
        TypeFrais typeFrais = typeFraisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypeFrais", id));
        return typeFraisMapper.toResponseDTO(typeFrais);
    }

    @Transactional
    public TypeFraisResponseDTO create(TypeFraisRequestDTO dto) {
        TypeFrais typeFrais = typeFraisMapper.toEntity(dto);
        return typeFraisMapper.toResponseDTO(typeFraisRepository.save(typeFrais));
    }

    @Transactional
    public TypeFraisResponseDTO update(UUID id, TypeFraisRequestDTO dto) {
        TypeFrais typeFrais = typeFraisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TypeFrais", id));
        typeFraisMapper.updateEntity(dto, typeFrais);
        return typeFraisMapper.toResponseDTO(typeFraisRepository.save(typeFrais));
    }

    @Transactional
    public void delete(UUID id) {
        if (!typeFraisRepository.existsById(id)) {
            throw new ResourceNotFoundException("TypeFrais", id);
        }
        typeFraisRepository.deleteById(id);
    }
}
