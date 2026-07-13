package com.schoolSys.schooolSys.facture;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.facture.dto.*;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final FactureLigneRepository factureLigneRepository;
    private final EcheancierRepository echeancierRepository;
    private final EcheanceRepository echeanceRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserContext currentUser;

    // ── Factures ──────────────────────────────────────────────

    public List<FactureResponseDTO> getAllFactures() {
        // Parents only see their own children's invoices.
        if (currentUser.hasRole(com.schoolSys.schooolSys.auth.UserRole.PARENT)) {
            var ownIds = currentUser.getScopedStudentIdsForParent();
            return factureRepository.findAll().stream()
                    .filter(f -> f.getStudent() != null && ownIds.contains(f.getStudent().getId()))
                    .map(this::toFactureDto)
                    .collect(Collectors.toList());
        }
        return factureRepository.findAll().stream()
                .map(this::toFactureDto)
                .collect(Collectors.toList());
    }

    public FactureResponseDTO getFactureById(UUID id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture", id));
        if (facture.getStudent() != null) {
            currentUser.assertCanAccessStudent(facture.getStudent().getId());
        }
        return toFactureDto(facture);
    }

    public List<FactureResponseDTO> getFacturesByStudent(UUID studentId) {
        currentUser.assertCanAccessStudent(studentId);
        return factureRepository.findByStudentId(studentId).stream()
                .map(this::toFactureDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FactureResponseDTO createFacture(FactureRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));

        Facture facture = Facture.builder()
                .numero(generateFactureNumero())
                .student(student)
                .dateEmission(dto.getDateEmission() != null ? dto.getDateEmission() : LocalDate.now())
                .dateEcheance(dto.getDateEcheance())
                .statut(dto.getStatut() != null ? dto.getStatut() : "BROUILLON")
                .notes(dto.getNotes())
                .build();

        if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (FactureLigneDTO ligneDto : dto.getLignes()) {
                int qty = ligneDto.getQuantite() != null ? ligneDto.getQuantite() : 1;
                BigDecimal montant = ligneDto.getPrixUnitaire().multiply(BigDecimal.valueOf(qty));
                FactureLigne ligne = FactureLigne.builder()
                        .facture(facture)
                        .designation(ligneDto.getDesignation())
                        .quantite(qty)
                        .prixUnitaire(ligneDto.getPrixUnitaire())
                        .montant(montant)
                        .build();
                facture.getLignes().add(ligne);
                total = total.add(montant);
            }
            facture.setMontantTotal(total);
        }

        return toFactureDto(factureRepository.save(facture));
    }

    @Transactional
    public FactureResponseDTO updateFacture(UUID id, FactureRequestDTO dto) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture", id));

        if (dto.getStudentId() != null) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
            facture.setStudent(student);
        }

        if (dto.getDateEmission() != null) {
            facture.setDateEmission(dto.getDateEmission());
        }
        facture.setDateEcheance(dto.getDateEcheance());
        if (dto.getStatut() != null) {
            facture.setStatut(dto.getStatut());
        }
        facture.setNotes(dto.getNotes());

        if (dto.getLignes() != null) {
            facture.getLignes().clear();
            BigDecimal total = BigDecimal.ZERO;
            for (FactureLigneDTO ligneDto : dto.getLignes()) {
                int qty = ligneDto.getQuantite() != null ? ligneDto.getQuantite() : 1;
                BigDecimal montant = ligneDto.getPrixUnitaire().multiply(BigDecimal.valueOf(qty));
                FactureLigne ligne = FactureLigne.builder()
                        .facture(facture)
                        .designation(ligneDto.getDesignation())
                        .quantite(qty)
                        .prixUnitaire(ligneDto.getPrixUnitaire())
                        .montant(montant)
                        .build();
                facture.getLignes().add(ligne);
                total = total.add(montant);
            }
            facture.setMontantTotal(total);
        }

        return toFactureDto(factureRepository.save(facture));
    }

    @Transactional
    public void deleteFacture(UUID id) {
        if (!factureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Facture", id);
        }
        factureRepository.deleteById(id);
    }

    // ── Echeanciers ───────────────────────────────────────────

    public List<EcheancierResponseDTO> getAllEcheanciers() {
        return echeancierRepository.findAll().stream()
                .map(this::toEcheancierDto)
                .collect(Collectors.toList());
    }

    public EcheancierResponseDTO getEcheancierById(UUID id) {
        Echeancier echeancier = echeancierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Echeancier", id));
        return toEcheancierDto(echeancier);
    }

    public List<EcheancierResponseDTO> getEcheanciersByStudent(UUID studentId) {
        return echeancierRepository.findByStudentId(studentId).stream()
                .map(this::toEcheancierDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EcheancierResponseDTO createEcheancier(EcheancierRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));

        Echeancier echeancier = Echeancier.builder()
                .student(student)
                .anneeScolaire(dto.getAnneeScolaire())
                .montantTotal(dto.getMontantTotal())
                .nombreEcheances(dto.getNombreEcheances())
                .statut(dto.getStatut() != null ? dto.getStatut() : "ACTIF")
                .notes(dto.getNotes())
                .build();

        // Auto-generate echeances with equal amounts spread monthly
        BigDecimal montantParEcheance = dto.getMontantTotal()
                .divide(BigDecimal.valueOf(dto.getNombreEcheances()), 2, RoundingMode.HALF_UP);
        LocalDate dateDebut = LocalDate.now().withDayOfMonth(1);

        List<Echeance> echeances = new ArrayList<>();
        for (int i = 1; i <= dto.getNombreEcheances(); i++) {
            Echeance echeance = Echeance.builder()
                    .echeancier(echeancier)
                    .numero(i)
                    .dateEcheance(dateDebut.plusMonths(i - 1))
                    .montant(montantParEcheance)
                    .build();
            echeances.add(echeance);
        }
        echeancier.setEcheances(echeances);

        return toEcheancierDto(echeancierRepository.save(echeancier));
    }

    @Transactional
    public EcheancierResponseDTO updateEcheancier(UUID id, EcheancierRequestDTO dto) {
        Echeancier echeancier = echeancierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Echeancier", id));

        if (dto.getStudentId() != null) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
            echeancier.setStudent(student);
        }

        echeancier.setAnneeScolaire(dto.getAnneeScolaire());
        echeancier.setMontantTotal(dto.getMontantTotal());
        echeancier.setNombreEcheances(dto.getNombreEcheances());
        if (dto.getStatut() != null) {
            echeancier.setStatut(dto.getStatut());
        }
        echeancier.setNotes(dto.getNotes());

        return toEcheancierDto(echeancierRepository.save(echeancier));
    }

    @Transactional
    public EcheanceDTO payerEcheance(UUID echeanceId, BigDecimal montant) {
        Echeance echeance = echeanceRepository.findById(echeanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Echeance", echeanceId));

        echeance.setMontantPaye(echeance.getMontantPaye().add(montant));
        echeance.setDatePaiement(LocalDate.now());

        if (echeance.getMontantPaye().compareTo(echeance.getMontant()) >= 0) {
            echeance.setStatut("PAYE");
        } else {
            echeance.setStatut("PARTIEL");
        }

        return toEcheanceDto(echeanceRepository.save(echeance));
    }

    @Transactional
    public void deleteEcheancier(UUID id) {
        if (!echeancierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Echeancier", id);
        }
        echeancierRepository.deleteById(id);
    }

    // ── Mappers ───────────────────────────────────────────────

    private FactureResponseDTO toFactureDto(Facture f) {
        Student s = f.getStudent();
        List<FactureLigneDTO> lignes = f.getLignes() != null
                ? f.getLignes().stream().map(this::toLigneDto).collect(Collectors.toList())
                : List.of();

        return FactureResponseDTO.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .studentId(s.getId())
                .studentNom(s.getLastName() + " " + s.getFirstName())
                .dateEmission(f.getDateEmission())
                .dateEcheance(f.getDateEcheance())
                .montantTotal(f.getMontantTotal())
                .montantPaye(f.getMontantPaye())
                .statut(f.getStatut())
                .notes(f.getNotes())
                .lignes(lignes)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    private FactureLigneDTO toLigneDto(FactureLigne l) {
        return FactureLigneDTO.builder()
                .id(l.getId())
                .designation(l.getDesignation())
                .quantite(l.getQuantite())
                .prixUnitaire(l.getPrixUnitaire())
                .montant(l.getMontant())
                .build();
    }

    private EcheancierResponseDTO toEcheancierDto(Echeancier e) {
        Student s = e.getStudent();
        List<EcheanceDTO> echeances = e.getEcheances() != null
                ? e.getEcheances().stream().map(this::toEcheanceDto).collect(Collectors.toList())
                : List.of();

        return EcheancierResponseDTO.builder()
                .id(e.getId())
                .studentId(s.getId())
                .studentNom(s.getLastName() + " " + s.getFirstName())
                .anneeScolaire(e.getAnneeScolaire())
                .montantTotal(e.getMontantTotal())
                .nombreEcheances(e.getNombreEcheances())
                .statut(e.getStatut())
                .notes(e.getNotes())
                .echeances(echeances)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private EcheanceDTO toEcheanceDto(Echeance e) {
        return EcheanceDTO.builder()
                .id(e.getId())
                .numero(e.getNumero())
                .dateEcheance(e.getDateEcheance())
                .montant(e.getMontant())
                .montantPaye(e.getMontantPaye())
                .statut(e.getStatut())
                .datePaiement(e.getDatePaiement())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private String generateFactureNumero() {
        return "FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
