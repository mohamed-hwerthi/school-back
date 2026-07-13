package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.transport.dto.ArretDTO;
import com.schoolSys.schooolSys.transport.dto.CircuitDTO;
import com.schoolSys.schooolSys.transport.dto.CreateCircuitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CircuitService {

    private final CircuitRepository circuitRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ArretRepository arretRepository;
    private final AffectationTransportRepository affectationRepository;

    public List<CircuitDTO> getAll() {
        return circuitRepository.findAllWithVehicule().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CircuitDTO getById(UUID id) {
        Circuit circuit = circuitRepository.findByIdWithDetails(id);
        if (circuit == null) {
            throw new ResourceNotFoundException("Circuit", id);
        }
        return toDto(circuit);
    }

    public List<ArretDTO> getArretsByCircuit(UUID circuitId) {
        if (!circuitRepository.existsById(circuitId)) {
            throw new ResourceNotFoundException("Circuit", circuitId);
        }
        return arretRepository.findByCircuitIdOrderByOrdreAsc(circuitId).stream()
                .map(this::toArretDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CircuitDTO create(CreateCircuitRequest request) {
        Circuit circuit = Circuit.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .heureDepart(request.getHeureDepart())
                .heureRetour(request.getHeureRetour())
                .distanceKm(request.getDistanceKm())
                .coutMensuel(request.getCoutMensuel())
                .arrets(new ArrayList<>())
                .build();

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicule", request.getVehiculeId()));
            circuit.setVehicule(vehicule);
        }

        Circuit saved = circuitRepository.save(circuit);

        if (request.getArrets() != null && !request.getArrets().isEmpty()) {
            for (ArretDTO arretDto : request.getArrets()) {
                Arret arret = Arret.builder()
                        .circuit(saved)
                        .nom(arretDto.getNom())
                        .adresse(arretDto.getAdresse())
                        .ordre(arretDto.getOrdre())
                        .heurePassage(arretDto.getHeurePassage())
                        .latitude(arretDto.getLatitude())
                        .longitude(arretDto.getLongitude())
                        .build();
                saved.getArrets().add(arret);
            }
            saved = circuitRepository.save(saved);
        }

        return toDto(saved);
    }

    @Transactional
    public CircuitDTO update(UUID id, CreateCircuitRequest request) {
        Circuit circuit = circuitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Circuit", id));

        circuit.setNom(request.getNom());
        circuit.setDescription(request.getDescription());
        circuit.setHeureDepart(request.getHeureDepart());
        circuit.setHeureRetour(request.getHeureRetour());
        circuit.setDistanceKm(request.getDistanceKm());
        circuit.setCoutMensuel(request.getCoutMensuel());

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicule", request.getVehiculeId()));
            circuit.setVehicule(vehicule);
        } else {
            circuit.setVehicule(null);
        }

        // Replace arrets
        circuit.getArrets().clear();
        if (request.getArrets() != null) {
            for (ArretDTO arretDto : request.getArrets()) {
                Arret arret = Arret.builder()
                        .circuit(circuit)
                        .nom(arretDto.getNom())
                        .adresse(arretDto.getAdresse())
                        .ordre(arretDto.getOrdre())
                        .heurePassage(arretDto.getHeurePassage())
                        .latitude(arretDto.getLatitude())
                        .longitude(arretDto.getLongitude())
                        .build();
                circuit.getArrets().add(arret);
            }
        }

        return toDto(circuitRepository.save(circuit));
    }

    @Transactional
    public void delete(UUID id) {
        if (!circuitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Circuit", id);
        }
        circuitRepository.deleteById(id);
    }

    private CircuitDTO toDto(Circuit c) {
        List<ArretDTO> arretDtos = c.getArrets() != null
                ? c.getArrets().stream().map(this::toArretDto).collect(Collectors.toList())
                : List.of();

        long nbEleves = affectationRepository.countByCircuitIdAndActifTrue(c.getId());

        return CircuitDTO.builder()
                .id(c.getId())
                .nom(c.getNom())
                .description(c.getDescription())
                .vehiculeId(c.getVehicule() != null ? c.getVehicule().getId() : null)
                .vehiculeImmatriculation(c.getVehicule() != null ? c.getVehicule().getImmatriculation() : null)
                .heureDepart(c.getHeureDepart())
                .heureRetour(c.getHeureRetour())
                .distanceKm(c.getDistanceKm())
                .coutMensuel(c.getCoutMensuel())
                .actif(c.getActif())
                .arrets(arretDtos)
                .nbEleves(nbEleves)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ArretDTO toArretDto(Arret a) {
        return ArretDTO.builder()
                .id(a.getId())
                .circuitId(a.getCircuit() != null ? a.getCircuit().getId() : null)
                .nom(a.getNom())
                .adresse(a.getAdresse())
                .ordre(a.getOrdre())
                .heurePassage(a.getHeurePassage())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .build();
    }
}
