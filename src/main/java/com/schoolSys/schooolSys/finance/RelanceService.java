package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.finance.dto.RelanceRequestDTO;
import com.schoolSys.schooolSys.finance.dto.RelanceResponseDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelanceService {

    private final AnneeScolaireProvider anneeScolaireProvider;
    private final RelanceRepository relanceRepository;
    private final PaiementRepository paiementRepository;
    private final StudentRepository studentRepository;
    private final RelanceMapper relanceMapper;

    public List<RelanceResponseDTO> findByAnneeScolaire(String anneeScolaire) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return relanceMapper.toResponseDTOList(
                relanceRepository.findByAnneeScolaireOrderByCreatedAtDesc(annee));
    }

    public List<RelanceResponseDTO> findByStudent(UUID studentId, String anneeScolaire) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return relanceMapper.toResponseDTOList(
                relanceRepository.findByStudentIdAndAnneeScolaireOrderByCreatedAtDesc(studentId, annee));
    }

    public List<RelanceResponseDTO> findPending(String anneeScolaire) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return relanceMapper.toResponseDTOList(
                relanceRepository.findByStatutAndAnneeScolaireOrderByDatePrevueAsc(
                        Relance.StatutRelance.EN_ATTENTE, annee));
    }

    public RelanceResponseDTO findById(UUID id) {
        Relance r = relanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relance", id));
        return relanceMapper.toResponseDTO(r);
    }

    @Transactional
    public RelanceResponseDTO create(RelanceRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Eleve", dto.getStudentId()));

        Paiement paiement = null;
        if (dto.getPaiementId() != null) {
            paiement = paiementRepository.findById(dto.getPaiementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paiement", dto.getPaiementId()));
        }

        String annee = anneeScolaireProvider.resolveAnneeScolaire(dto.getAnneeScolaire());

        Integer maxNum = relanceRepository.findMaxNumeroRelanceByStudentAndAnnee(student.getId(), annee);
        int numero = dto.getNumeroRelance() != null ? dto.getNumeroRelance() : (maxNum != null ? maxNum + 1 : 1);

        String destinataire = dto.getDestinataire();
        if (destinataire == null || destinataire.isBlank()) {
            destinataire = resolveDestinataire(student, dto.getType());
        }

        Relance relance = Relance.builder()
                .student(student)
                .paiement(paiement)
                .type(dto.getType())
                .statut(Relance.StatutRelance.EN_ATTENTE)
                .message(dto.getMessage())
                .destinataire(destinataire)
                .montantDu(dto.getMontantDu())
                .datePrevue(dto.getDatePrevue() != null ? dto.getDatePrevue() : LocalDate.now())
                .anneeScolaire(annee)
                .numeroRelance(numero)
                .build();

        return relanceMapper.toResponseDTO(relanceRepository.save(relance));
    }

    @Transactional
    public RelanceResponseDTO markAsEnvoyee(UUID id) {
        Relance r = relanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relance", id));
        r.setStatut(Relance.StatutRelance.ENVOYEE);
        r.setDateEnvoi(LocalDate.now());
        return relanceMapper.toResponseDTO(relanceRepository.save(r));
    }

    @Transactional
    public RelanceResponseDTO markAsEchouee(UUID id) {
        Relance r = relanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relance", id));
        r.setStatut(Relance.StatutRelance.ECHOUEE);
        return relanceMapper.toResponseDTO(relanceRepository.save(r));
    }

    @Transactional
    public void delete(UUID id) {
        if (!relanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relance", id);
        }
        relanceRepository.deleteById(id);
    }

    /**
     * Generates relances for all overdue/pending payments in the given year.
     * Only creates a new relance if no pending relance already exists for that student+paiement.
     */
    @Transactional
    public List<RelanceResponseDTO> generateRelances(String anneeScolaire, Relance.TypeRelance type) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);

        List<Paiement> overdue = paiementRepository.findByAnneeScolaireAndStatutIn(
                annee, List.of(Paiement.StatutPaiement.EN_RETARD, Paiement.StatutPaiement.EN_ATTENTE));

        List<Relance> generated = new ArrayList<>();

        for (Paiement p : overdue) {
            // Skip if a pending relance already exists for this student+paiement
            if (relanceRepository.existsByStudentIdAndPaiementIdAndStatut(
                    p.getStudent().getId(), p.getId(), Relance.StatutRelance.EN_ATTENTE)) {
                continue;
            }

            Student student = p.getStudent();
            String destinataire = resolveDestinataire(student, type);

            Integer maxNum = relanceRepository.findMaxNumeroRelanceByStudentAndAnnee(student.getId(), annee);
            int numero = maxNum != null ? maxNum + 1 : 1;

            BigDecimal solde = p.getMontantDu().subtract(p.getMontantPaye());

            String message = buildMessage(student, p, solde, numero);

            Relance relance = Relance.builder()
                    .student(student)
                    .paiement(p)
                    .type(type)
                    .statut(Relance.StatutRelance.EN_ATTENTE)
                    .message(message)
                    .destinataire(destinataire)
                    .montantDu(solde)
                    .datePrevue(LocalDate.now())
                    .anneeScolaire(annee)
                    .numeroRelance(numero)
                    .build();

            generated.add(relanceRepository.save(relance));
        }

        return relanceMapper.toResponseDTOList(generated);
    }

    public RelanceStatsDTO getStats(String anneeScolaire) {
        String annee = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        long total = relanceRepository.countByAnneeScolaire(annee);
        long enAttente = relanceRepository.countByAnneeScolaireAndStatut(annee, Relance.StatutRelance.EN_ATTENTE);
        long envoyees = relanceRepository.countByAnneeScolaireAndStatut(annee, Relance.StatutRelance.ENVOYEE);
        long echouees = relanceRepository.countByAnneeScolaireAndStatut(annee, Relance.StatutRelance.ECHOUEE);
        return new RelanceStatsDTO(total, enAttente, envoyees, echouees);
    }

    // ── helpers ──

    private String resolveDestinataire(Student student, Relance.TypeRelance type) {
        return switch (type) {
            case EMAIL -> student.getParentEmail() != null ? student.getParentEmail() : student.getEmail();
            case SMS -> student.getParentPhone() != null ? student.getParentPhone() : "";
            case COURRIER -> student.getAddress() != null ? student.getAddress() : "";
        };
    }

    private String buildMessage(Student student, Paiement paiement, BigDecimal solde, int numero) {
        String prenom = student.getFirstName();
        String nom = student.getLastName();
        String mois = paiement.getMois();
        return String.format(
                "Relance N°%d - Cher(e) parent de %s %s, nous vous rappelons qu'un montant de %.2f TND " +
                "reste impaye pour le mois de %s. Merci de regulariser votre situation dans les plus brefs delais.",
                numero, prenom, nom, solde, mois);
    }

    // ── Stats DTO ──

    public record RelanceStatsDTO(long total, long enAttente, long envoyees, long echouees) {}
}
