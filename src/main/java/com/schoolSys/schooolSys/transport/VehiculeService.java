package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.transport.dto.VehiculeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;

    public List<VehiculeDTO> getAll() {
        return vehiculeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public VehiculeDTO getById(UUID id) {
        return toDto(vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id)));
    }

    @Transactional
    public VehiculeDTO create(VehiculeDTO dto) {
        Vehicule vehicule = Vehicule.builder()
                .immatriculation(dto.getImmatriculation())
                .marque(dto.getMarque())
                .modele(dto.getModele())
                .capacite(dto.getCapacite())
                .chauffeurNom(dto.getChauffeurNom())
                .chauffeurTelephone(dto.getChauffeurTelephone())
                .dateAssurance(dto.getDateAssurance())
                .dateControleTechnique(dto.getDateControleTechnique())
                .statut(dto.getStatut() != null ? dto.getStatut() : "ACTIF")
                .build();
        return toDto(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public VehiculeDTO update(UUID id, VehiculeDTO dto) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
        if (dto.getImmatriculation() != null) vehicule.setImmatriculation(dto.getImmatriculation());
        if (dto.getMarque() != null) vehicule.setMarque(dto.getMarque());
        if (dto.getModele() != null) vehicule.setModele(dto.getModele());
        if (dto.getCapacite() != null) vehicule.setCapacite(dto.getCapacite());
        if (dto.getChauffeurNom() != null) vehicule.setChauffeurNom(dto.getChauffeurNom());
        if (dto.getChauffeurTelephone() != null) vehicule.setChauffeurTelephone(dto.getChauffeurTelephone());
        if (dto.getDateAssurance() != null) vehicule.setDateAssurance(dto.getDateAssurance());
        if (dto.getDateControleTechnique() != null) vehicule.setDateControleTechnique(dto.getDateControleTechnique());
        if (dto.getStatut() != null) vehicule.setStatut(dto.getStatut());
        return toDto(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public void delete(UUID id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }

    private VehiculeDTO toDto(Vehicule v) {
        return VehiculeDTO.builder()
                .id(v.getId())
                .immatriculation(v.getImmatriculation())
                .marque(v.getMarque())
                .modele(v.getModele())
                .capacite(v.getCapacite())
                .chauffeurNom(v.getChauffeurNom())
                .chauffeurTelephone(v.getChauffeurTelephone())
                .dateAssurance(v.getDateAssurance())
                .dateControleTechnique(v.getDateControleTechnique())
                .statut(v.getStatut())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
