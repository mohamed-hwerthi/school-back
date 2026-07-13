package com.schoolSys.schooolSys.depense;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.depense.dto.CategorieDepenseRequestDTO;
import com.schoolSys.schooolSys.depense.dto.CategorieDepenseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategorieDepenseService {

    private final CategorieDepenseRepository categorieRepository;
    private final CategorieDepenseMapper categorieMapper;

    public List<CategorieDepenseResponseDTO> findAll() {
        return categorieRepository.findAll().stream()
                .map(categorieMapper::toResponseDTO)
                .toList();
    }

    public CategorieDepenseResponseDTO findById(UUID id) {
        CategorieDepense cat = categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategorieDepense", id));
        return categorieMapper.toResponseDTO(cat);
    }

    @Transactional
    public CategorieDepenseResponseDTO create(CategorieDepenseRequestDTO dto) {
        CategorieDepense cat = categorieMapper.toEntity(dto);
        return categorieMapper.toResponseDTO(categorieRepository.save(cat));
    }

    @Transactional
    public CategorieDepenseResponseDTO update(UUID id, CategorieDepenseRequestDTO dto) {
        CategorieDepense cat = categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CategorieDepense", id));
        categorieMapper.updateEntity(dto, cat);
        return categorieMapper.toResponseDTO(categorieRepository.save(cat));
    }

    @Transactional
    public void delete(UUID id) {
        if (!categorieRepository.existsById(id)) {
            throw new ResourceNotFoundException("CategorieDepense", id);
        }
        categorieRepository.deleteById(id);
    }
}
