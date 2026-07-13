package com.schoolSys.schooolSys.cantine;

import java.util.UUID;

import com.schoolSys.schooolSys.cantine.dto.AbonnementCantineDTO;
import com.schoolSys.schooolSys.cantine.dto.CreateAbonnementRequest;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AbonnementCantineService {

    private final AbonnementCantineRepository abonnementRepository;

    public List<AbonnementCantineDTO> getAll() {
        return abonnementRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AbonnementCantineDTO getById(UUID id) {
        return toDto(abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AbonnementCantine", id)));
    }

    public List<AbonnementCantineDTO> getActifs() {
        return abonnementRepository.findByActifTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AbonnementCantineDTO> getByEleve(UUID eleveId) {
        return abonnementRepository.findByEleveId(eleveId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AbonnementCantineDTO create(CreateAbonnementRequest request) {
        AbonnementCantine abonnement = AbonnementCantine.builder()
                .eleveId(request.getEleveId())
                .typeAbonnement(request.getTypeAbonnement())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .montant(request.getMontant())
                .allergies(request.getAllergies())
                .regime(request.getRegime() != null ? request.getRegime() : "STANDARD")
                .build();
        return toDto(abonnementRepository.save(abonnement));
    }

    @Transactional
    public AbonnementCantineDTO update(UUID id, CreateAbonnementRequest request) {
        AbonnementCantine abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AbonnementCantine", id));
        if (request.getEleveId() != null) abonnement.setEleveId(request.getEleveId());
        if (request.getTypeAbonnement() != null) abonnement.setTypeAbonnement(request.getTypeAbonnement());
        if (request.getDateDebut() != null) abonnement.setDateDebut(request.getDateDebut());
        abonnement.setDateFin(request.getDateFin());
        if (request.getMontant() != null) abonnement.setMontant(request.getMontant());
        abonnement.setAllergies(request.getAllergies());
        if (request.getRegime() != null) abonnement.setRegime(request.getRegime());
        return toDto(abonnementRepository.save(abonnement));
    }

    @Transactional
    public AbonnementCantineDTO deactivate(UUID id) {
        AbonnementCantine abonnement = abonnementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AbonnementCantine", id));
        abonnement.setActif(false);
        return toDto(abonnementRepository.save(abonnement));
    }

    @Transactional
    public void delete(UUID id) {
        if (!abonnementRepository.existsById(id)) {
            throw new ResourceNotFoundException("AbonnementCantine", id);
        }
        abonnementRepository.deleteById(id);
    }

    private AbonnementCantineDTO toDto(AbonnementCantine a) {
        return AbonnementCantineDTO.builder()
                .id(a.getId())
                .eleveId(a.getEleveId())
                .typeAbonnement(a.getTypeAbonnement())
                .dateDebut(a.getDateDebut())
                .dateFin(a.getDateFin())
                .montant(a.getMontant())
                .actif(a.getActif())
                .allergies(a.getAllergies())
                .regime(a.getRegime())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
