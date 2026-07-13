package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.PaiementRequestDTO;
import com.schoolSys.schooolSys.finance.dto.PaiementResponseDTO;
import com.schoolSys.schooolSys.finance.dto.FinanceDashboardDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final PaiementMapper paiementMapper;
    private final TypeFraisRepository typeFraisRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    public PagedResponse<PaiementResponseDTO> findAll(
            String search,
            String anneeScolaire,
            String mois,
            Paiement.StatutPaiement statut,
            Paiement.ModePaiement modePaiement,
            UUID studentId,
            UUID typeFraisId,
            Pageable pageable
    ) {
        Specification<Paiement> spec = Specification
                .where(PaiementSpecification.searchStudent(search))
                .and(PaiementSpecification.hasAnneeScolaire(anneeScolaire))
                .and(PaiementSpecification.hasMois(mois))
                .and(PaiementSpecification.hasStatut(statut))
                .and(PaiementSpecification.hasModePaiement(modePaiement))
                .and(PaiementSpecification.hasStudentId(studentId))
                .and(PaiementSpecification.hasTypeFraisId(typeFraisId));

        Page<Paiement> page = paiementRepository.findAll(spec, pageable);
        List<PaiementResponseDTO> content = paiementMapper.toResponseDTOList(page.getContent());
        return PagedResponse.from(page, content);
    }

    public PaiementResponseDTO findById(UUID id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));
        return paiementMapper.toResponseDTO(paiement);
    }

    public List<PaiementResponseDTO> findByStudentId(UUID studentId) {
        return paiementMapper.toResponseDTOList(paiementRepository.findByStudentId(studentId));
    }

    public List<PaiementResponseDTO> findByStudentIdAndAnneeScolaire(UUID studentId, String anneeScolaire) {
        return paiementMapper.toResponseDTOList(
                paiementRepository.findByStudentIdAndAnneeScolaire(studentId, anneeScolaire));
    }

    @Transactional
    public PaiementResponseDTO create(PaiementRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
        TypeFrais typeFrais = typeFraisRepository.findById(dto.getTypeFraisId())
                .orElseThrow(() -> new ResourceNotFoundException("TypeFrais", dto.getTypeFraisId()));

        Paiement paiement = Paiement.builder()
                .student(student)
                .typeFrais(typeFrais)
                .mois(dto.getMois())
                .anneeScolaire(dto.getAnneeScolaire())
                .montantDu(dto.getMontantDu())
                .montantPaye(dto.getMontantPaye())
                .datePaiement(dto.getDatePaiement() != null ? dto.getDatePaiement() : LocalDate.now())
                .modePaiement(dto.getModePaiement())
                .statut(computeStatut(dto.getMontantPaye(), dto.getMontantDu()))
                .reference(dto.getReference() != null ? dto.getReference() : generateReference())
                .notes(dto.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Paiement saved = paiementRepository.save(paiement);
        auditService.log("CREATE", "PAIEMENT", saved.getId(),
                "Paiement de " + saved.getMontantPaye() + " (élève #" + student.getId() + ")");
        return paiementMapper.toResponseDTO(saved);
    }

    @Transactional
    public PaiementResponseDTO update(UUID id, PaiementRequestDTO dto) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", id));

        if (dto.getStudentId() != null) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
            paiement.setStudent(student);
        }
        if (dto.getTypeFraisId() != null) {
            TypeFrais typeFrais = typeFraisRepository.findById(dto.getTypeFraisId())
                    .orElseThrow(() -> new ResourceNotFoundException("TypeFrais", dto.getTypeFraisId()));
            paiement.setTypeFrais(typeFrais);
        }

        paiement.setMois(dto.getMois());
        paiement.setAnneeScolaire(dto.getAnneeScolaire());
        paiement.setMontantDu(dto.getMontantDu());
        paiement.setMontantPaye(dto.getMontantPaye());
        paiement.setDatePaiement(dto.getDatePaiement());
        paiement.setModePaiement(dto.getModePaiement());
        paiement.setStatut(computeStatut(dto.getMontantPaye(), dto.getMontantDu()));
        paiement.setNotes(dto.getNotes());

        Paiement saved = paiementRepository.save(paiement);
        auditService.log("UPDATE", "PAIEMENT", saved.getId(),
                "Modification du paiement #" + saved.getId());
        return paiementMapper.toResponseDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!paiementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paiement", id);
        }
        paiementRepository.deleteById(id);
        auditService.log("DELETE", "PAIEMENT", id, "Suppression d'un paiement");
    }

    public FinanceDashboardDTO getDashboard(String anneeScolaire) {
        BigDecimal totalEncaisse = paiementRepository.sumMontantPayeByAnneeScolaire(anneeScolaire);
        BigDecimal totalDu = paiementRepository.sumMontantDuByAnneeScolaire(anneeScolaire);
        BigDecimal totalImpayes = paiementRepository.sumImpayes(anneeScolaire);

        long totalPaiements = paiementRepository.findByAnneeScolaire(anneeScolaire).size();
        long paiementsPayes = paiementRepository.countByStatutAndAnneeScolaire(
                Paiement.StatutPaiement.PAYE, anneeScolaire);
        long paiementsEnRetard = paiementRepository.countByStatutAndAnneeScolaire(
                Paiement.StatutPaiement.EN_RETARD, anneeScolaire);
        long paiementsPartiels = paiementRepository.countByStatutAndAnneeScolaire(
                Paiement.StatutPaiement.PARTIEL, anneeScolaire);
        long paiementsEnAttente = paiementRepository.countByStatutAndAnneeScolaire(
                Paiement.StatutPaiement.EN_ATTENTE, anneeScolaire);

        BigDecimal tauxRecouvrement = totalDu.compareTo(BigDecimal.ZERO) > 0
                ? totalEncaisse.multiply(BigDecimal.valueOf(100)).divide(totalDu, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return FinanceDashboardDTO.builder()
                .totalEncaisse(totalEncaisse)
                .totalDu(totalDu)
                .totalImpayes(totalImpayes)
                .tauxRecouvrement(tauxRecouvrement)
                .totalPaiements(totalPaiements)
                .paiementsPayes(paiementsPayes)
                .paiementsEnRetard(paiementsEnRetard)
                .paiementsPartiels(paiementsPartiels)
                .paiementsEnAttente(paiementsEnAttente)
                .build();
    }

    private Paiement.StatutPaiement computeStatut(BigDecimal montantPaye, BigDecimal montantDu) {
        if (montantPaye.compareTo(montantDu) >= 0) {
            return Paiement.StatutPaiement.PAYE;
        } else if (montantPaye.compareTo(BigDecimal.ZERO) > 0) {
            return Paiement.StatutPaiement.PARTIEL;
        } else {
            return Paiement.StatutPaiement.EN_ATTENTE;
        }
    }

    private String generateReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
