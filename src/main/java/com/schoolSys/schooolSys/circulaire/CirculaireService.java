package com.schoolSys.schooolSys.circulaire;

import java.util.UUID;

import com.schoolSys.schooolSys.circulaire.dto.CirculaireRequestDTO;
import com.schoolSys.schooolSys.circulaire.dto.CirculaireResponseDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CirculaireService {

    private final CirculaireRepository circulaireRepository;
    private final CirculaireMapper circulaireMapper;

    public List<CirculaireResponseDTO> findAll(String type, String statut, String cible, String search) {
        List<Circulaire> circulaires;

        if (search != null && !search.isBlank()) {
            circulaires = circulaireRepository.findByTitreContainingIgnoreCaseOrderByDateCreationDesc(search);
        } else if (type != null && !type.isBlank()) {
            circulaires = circulaireRepository.findByTypeOrderByDateCreationDesc(type);
        } else if (statut != null && !statut.isBlank()) {
            circulaires = circulaireRepository.findByStatutOrderByDateCreationDesc(statut);
        } else if (cible != null && !cible.isBlank()) {
            circulaires = circulaireRepository.findByCibleOrderByDateCreationDesc(cible);
        } else {
            circulaires = circulaireRepository.findAllByOrderByDateCreationDesc();
        }

        return circulaireMapper.toResponseDTOList(circulaires);
    }

    public CirculaireResponseDTO findById(UUID id) {
        Circulaire circulaire = circulaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Circulaire", id));
        return circulaireMapper.toResponseDTO(circulaire);
    }

    @Transactional
    public CirculaireResponseDTO create(CirculaireRequestDTO dto) {
        Circulaire circulaire = circulaireMapper.toEntity(dto);
        circulaire.setDateCreation(LocalDateTime.now());
        circulaire.setStatut("Brouillon");
        circulaire.setCreatedAt(LocalDateTime.now());
        circulaire.setUpdatedAt(LocalDateTime.now());
        return circulaireMapper.toResponseDTO(circulaireRepository.save(circulaire));
    }

    @Transactional
    public CirculaireResponseDTO update(UUID id, CirculaireRequestDTO dto) {
        Circulaire circulaire = circulaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Circulaire", id));
        circulaireMapper.updateEntity(dto, circulaire);
        circulaire.setUpdatedAt(LocalDateTime.now());
        return circulaireMapper.toResponseDTO(circulaireRepository.save(circulaire));
    }

    @Transactional
    public CirculaireResponseDTO publish(UUID id) {
        Circulaire circulaire = circulaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Circulaire", id));
        circulaire.setStatut("Publiée");
        circulaire.setDatePublication(LocalDateTime.now());
        circulaire.setUpdatedAt(LocalDateTime.now());
        return circulaireMapper.toResponseDTO(circulaireRepository.save(circulaire));
    }

    @Transactional
    public CirculaireResponseDTO archive(UUID id) {
        Circulaire circulaire = circulaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Circulaire", id));
        circulaire.setStatut("Archivée");
        circulaire.setUpdatedAt(LocalDateTime.now());
        return circulaireMapper.toResponseDTO(circulaireRepository.save(circulaire));
    }

    @Transactional
    public void delete(UUID id) {
        if (!circulaireRepository.existsById(id)) {
            throw new ResourceNotFoundException("Circulaire", id);
        }
        circulaireRepository.deleteById(id);
    }
}
