package com.schoolSys.schooolSys.rapport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.rapport.dto.RapportRequestDTO;
import com.schoolSys.schooolSys.rapport.dto.RapportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RapportService {

    private final RapportRepository rapportRepository;
    private final RapportMapper rapportMapper;

    public List<RapportResponseDTO> findAll(String type, String statut) {
        List<Rapport> rapports;

        if (type != null && !type.isBlank()) {
            rapports = rapportRepository.findByTypeOrderByDateGenerationDesc(type);
        } else if (statut != null && !statut.isBlank()) {
            rapports = rapportRepository.findByStatutOrderByDateGenerationDesc(statut);
        } else {
            rapports = rapportRepository.findAllByOrderByDateGenerationDesc();
        }

        return rapportMapper.toResponseDTOList(rapports);
    }

    public RapportResponseDTO findById(UUID id) {
        Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport", id));
        return rapportMapper.toResponseDTO(rapport);
    }

    @Transactional
    public RapportResponseDTO create(RapportRequestDTO dto) {
        Rapport rapport = rapportMapper.toEntity(dto);
        rapport.setDateGeneration(LocalDateTime.now());
        if (rapport.getStatut() == null || rapport.getStatut().isBlank()) {
            rapport.setStatut("Brouillon");
        }
        rapport.setCreatedAt(LocalDateTime.now());
        rapport.setUpdatedAt(LocalDateTime.now());
        return rapportMapper.toResponseDTO(rapportRepository.save(rapport));
    }

    @Transactional
    public RapportResponseDTO update(UUID id, RapportRequestDTO dto) {
        Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport", id));
        rapportMapper.updateEntity(dto, rapport);
        rapport.setUpdatedAt(LocalDateTime.now());
        return rapportMapper.toResponseDTO(rapportRepository.save(rapport));
    }

    @Transactional
    public void delete(UUID id) {
        if (!rapportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rapport", id);
        }
        rapportRepository.deleteById(id);
    }
}
