package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.rh.dto.CreateFichePaieRequest;
import com.schoolSys.schooolSys.rh.dto.FichePaieDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FichePaieService {

    private final FichePaieRepository fichePaieRepository;

    public List<FichePaieDTO> getAll() {
        return fichePaieRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FichePaieDTO getById(UUID id) {
        FichePaie f = fichePaieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FichePaie", id));
        return toDto(f);
    }

    public List<FichePaieDTO> getByEmploye(UUID employeId) {
        return fichePaieRepository.findByEmployeId(employeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FichePaieDTO> getByMoisAndAnnee(Integer mois, Integer annee) {
        return fichePaieRepository.findByMoisAndAnnee(mois, annee).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FichePaieDTO create(CreateFichePaieRequest dto) {
        FichePaie fichePaie = FichePaie.builder()
                .employeId(dto.getEmployeId())
                .employeType(dto.getEmployeType())
                .mois(dto.getMois())
                .annee(dto.getAnnee())
                .salaireBase(dto.getSalaireBase())
                .primes(dto.getPrimes() != null ? dto.getPrimes() : BigDecimal.ZERO)
                .retenues(dto.getRetenues() != null ? dto.getRetenues() : BigDecimal.ZERO)
                .salaireNet(dto.getSalaireNet())
                .datePaiement(dto.getDatePaiement())
                .paye(dto.getPaye() != null ? dto.getPaye() : false)
                .commentaire(dto.getCommentaire())
                .build();

        return toDto(fichePaieRepository.save(fichePaie));
    }

    @Transactional
    public FichePaieDTO update(UUID id, CreateFichePaieRequest dto) {
        FichePaie fichePaie = fichePaieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FichePaie", id));

        fichePaie.setEmployeId(dto.getEmployeId());
        fichePaie.setEmployeType(dto.getEmployeType());
        fichePaie.setMois(dto.getMois());
        fichePaie.setAnnee(dto.getAnnee());
        fichePaie.setSalaireBase(dto.getSalaireBase());
        if (dto.getPrimes() != null) {
            fichePaie.setPrimes(dto.getPrimes());
        }
        if (dto.getRetenues() != null) {
            fichePaie.setRetenues(dto.getRetenues());
        }
        fichePaie.setSalaireNet(dto.getSalaireNet());
        fichePaie.setDatePaiement(dto.getDatePaiement());
        if (dto.getPaye() != null) {
            fichePaie.setPaye(dto.getPaye());
        }
        fichePaie.setCommentaire(dto.getCommentaire());

        return toDto(fichePaieRepository.save(fichePaie));
    }

    @Transactional
    public void delete(UUID id) {
        if (!fichePaieRepository.existsById(id)) {
            throw new ResourceNotFoundException("FichePaie", id);
        }
        fichePaieRepository.deleteById(id);
    }

    public BigDecimal getMasseSalariale(Integer mois, Integer annee) {
        return fichePaieRepository.sumSalaireNetByMoisAndAnnee(mois, annee);
    }

    private FichePaieDTO toDto(FichePaie f) {
        return FichePaieDTO.builder()
                .id(f.getId())
                .employeId(f.getEmployeId())
                .employeType(f.getEmployeType())
                .mois(f.getMois())
                .annee(f.getAnnee())
                .salaireBase(f.getSalaireBase())
                .primes(f.getPrimes())
                .retenues(f.getRetenues())
                .salaireNet(f.getSalaireNet())
                .datePaiement(f.getDatePaiement())
                .paye(f.getPaye())
                .commentaire(f.getCommentaire())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
