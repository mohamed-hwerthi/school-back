package com.schoolSys.schooolSys.analytics;

import java.util.UUID;

import com.schoolSys.schooolSys.analytics.dto.KpiConfigRequestDTO;
import com.schoolSys.schooolSys.analytics.dto.KpiConfigResponseDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiConfigService {

    private final KpiConfigRepository kpiConfigRepository;

    public List<KpiConfigResponseDTO> findAll() {
        return kpiConfigRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public KpiConfigResponseDTO findById(UUID id) {
        KpiConfig config = kpiConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KpiConfig", id));
        return toResponseDTO(config);
    }

    @Transactional
    public KpiConfigResponseDTO create(KpiConfigRequestDTO dto) {
        KpiConfig config = KpiConfig.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .type(dto.getType())
                .valeurCible(dto.getValeurCible())
                .seuilAlerte(dto.getSeuilAlerte())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .build();
        return toResponseDTO(kpiConfigRepository.save(config));
    }

    @Transactional
    public KpiConfigResponseDTO update(UUID id, KpiConfigRequestDTO dto) {
        KpiConfig config = kpiConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KpiConfig", id));
        config.setNom(dto.getNom());
        config.setDescription(dto.getDescription());
        config.setType(dto.getType());
        config.setValeurCible(dto.getValeurCible());
        config.setSeuilAlerte(dto.getSeuilAlerte());
        if (dto.getActif() != null) {
            config.setActif(dto.getActif());
        }
        return toResponseDTO(kpiConfigRepository.save(config));
    }

    @Transactional
    public void delete(UUID id) {
        if (!kpiConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("KpiConfig", id);
        }
        kpiConfigRepository.deleteById(id);
    }

    private KpiConfigResponseDTO toResponseDTO(KpiConfig config) {
        return KpiConfigResponseDTO.builder()
                .id(config.getId())
                .nom(config.getNom())
                .description(config.getDescription())
                .type(config.getType())
                .valeurCible(config.getValeurCible())
                .seuilAlerte(config.getSeuilAlerte())
                .actif(config.getActif())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
