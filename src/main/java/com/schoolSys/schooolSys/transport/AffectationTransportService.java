package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.transport.dto.AffectationTransportDTO;
import com.schoolSys.schooolSys.transport.dto.CreateAffectationRequest;
import com.schoolSys.schooolSys.transport.dto.TransportStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffectationTransportService {

    private final AffectationTransportRepository affectationRepository;
    private final CircuitRepository circuitRepository;
    private final ArretRepository arretRepository;
    private final VehiculeRepository vehiculeRepository;

    public List<AffectationTransportDTO> getAll() {
        return affectationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AffectationTransportDTO> getByCircuit(UUID circuitId) {
        return affectationRepository.findByCircuitIdAndActifTrue(circuitId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AffectationTransportDTO> getByEleve(UUID eleveId) {
        return affectationRepository.findByEleveIdAndActifTrue(eleveId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AffectationTransportDTO affecter(CreateAffectationRequest request) {
        Circuit circuit = circuitRepository.findById(request.getCircuitId())
                .orElseThrow(() -> new ResourceNotFoundException("Circuit", request.getCircuitId()));

        AffectationTransport affectation = AffectationTransport.builder()
                .eleveId(request.getEleveId())
                .circuit(circuit)
                .anneeScolaire(request.getAnneeScolaire())
                .build();

        if (request.getArretId() != null) {
            Arret arret = arretRepository.findById(request.getArretId())
                    .orElseThrow(() -> new ResourceNotFoundException("Arret", request.getArretId()));
            affectation.setArret(arret);
        }

        return toDto(affectationRepository.save(affectation));
    }

    @Transactional
    public AffectationTransportDTO desaffecter(UUID id) {
        AffectationTransport affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AffectationTransport", id));
        affectation.setActif(false);
        return toDto(affectationRepository.save(affectation));
    }

    @Transactional
    public void delete(UUID id) {
        if (!affectationRepository.existsById(id)) {
            throw new ResourceNotFoundException("AffectationTransport", id);
        }
        affectationRepository.deleteById(id);
    }

    public TransportStatsDTO getStats() {
        long totalCircuits = circuitRepository.count();
        long totalVehicules = vehiculeRepository.count();
        long totalEleves = affectationRepository.countActifs();

        // Calculate fill rate: total assigned students / total vehicle capacity
        int totalCapacity = vehiculeRepository.findAll().stream()
                .mapToInt(v -> v.getCapacite() != null ? v.getCapacite() : 0)
                .sum();
        double tauxRemplissage = totalCapacity > 0
                ? Math.round((double) totalEleves / totalCapacity * 100.0 * 100.0) / 100.0
                : 0.0;

        return TransportStatsDTO.builder()
                .totalCircuits(totalCircuits)
                .totalVehicules(totalVehicules)
                .totalEleves(totalEleves)
                .tauxRemplissage(tauxRemplissage)
                .build();
    }

    private AffectationTransportDTO toDto(AffectationTransport a) {
        return AffectationTransportDTO.builder()
                .id(a.getId())
                .eleveId(a.getEleveId())
                .circuitId(a.getCircuit() != null ? a.getCircuit().getId() : null)
                .circuitNom(a.getCircuit() != null ? a.getCircuit().getNom() : null)
                .arretId(a.getArret() != null ? a.getArret().getId() : null)
                .arretNom(a.getArret() != null ? a.getArret().getNom() : null)
                .anneeScolaire(a.getAnneeScolaire())
                .actif(a.getActif())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
