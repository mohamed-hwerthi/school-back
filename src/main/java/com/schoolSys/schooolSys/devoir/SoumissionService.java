package com.schoolSys.schooolSys.devoir;

import java.util.UUID;

import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.devoir.dto.CorrectionRequest;
import com.schoolSys.schooolSys.devoir.dto.CreateSoumissionRequest;
import com.schoolSys.schooolSys.devoir.dto.DevoirStatsDTO;
import com.schoolSys.schooolSys.devoir.dto.SoumissionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoumissionService {

    private final SoumissionRepository soumissionRepository;
    private final DevoirRepository devoirRepository;
    private final CurrentUserContext currentUserContext;
    private final AnneeScolaireProvider anneeScolaireProvider;

    public List<SoumissionDTO> findByDevoir(UUID devoirId) {
        return soumissionRepository.findByDevoirIdOrderByDateSoumissionDesc(devoirId)
                .stream().map(this::toDTO).toList();
    }

    public List<SoumissionDTO> findByEleve(UUID eleveId) {
        currentUserContext.assertCanAccessStudent(eleveId);
        return soumissionRepository.findByEleveIdOrderByDateSoumissionDesc(eleveId)
                .stream().map(this::toDTO).toList();
    }

    public List<SoumissionDTO> findByEleveAndAnnee(UUID eleveId, String anneeScolaire) {
        currentUserContext.assertCanAccessStudent(eleveId);
        String year = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        return soumissionRepository.findByEleveIdAndAnneeScolaireOrderByDateSoumissionDesc(eleveId, year)
                .stream().map(this::toDTO).toList();
    }

    public SoumissionDTO findById(UUID id) {
        Soumission soumission = soumissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Soumission", id));
        return toDTO(soumission);
    }

    @Transactional
    public SoumissionDTO submit(CreateSoumissionRequest request) {
        Devoir devoir = devoirRepository.findById(request.getDevoirId())
                .orElseThrow(() -> new ResourceNotFoundException("Devoir", request.getDevoirId()));

        boolean enRetard = devoir.getDateLimite() != null && LocalDate.now().isAfter(devoir.getDateLimite());

        Soumission soumission = Soumission.builder()
                .devoir(devoir)
                .eleveId(request.getEleveId())
                .contenu(request.getContenu())
                .fichierUrl(request.getFichierUrl())
                .enRetard(enRetard)
                .anneeScolaire(anneeScolaireProvider.resolveAnneeScolaire(devoir.getAnneeScolaire()))
                .build();

        return toDTO(soumissionRepository.save(soumission));
    }

    @Transactional
    public SoumissionDTO correct(UUID id, CorrectionRequest request) {
        Soumission soumission = soumissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Soumission", id));

        soumission.setNote(request.getNote());
        soumission.setCommentaireCorrection(request.getCommentaire());
        soumission.setCorrige(true);
        soumission.setUpdatedAt(LocalDateTime.now());

        return toDTO(soumissionRepository.save(soumission));
    }

    public DevoirStatsDTO getStats(UUID devoirId) {
        devoirRepository.findById(devoirId)
                .orElseThrow(() -> new ResourceNotFoundException("Devoir", devoirId));

        long totalSoumissions = soumissionRepository.countByDevoirId(devoirId);
        long enRetard = soumissionRepository.countByDevoirIdAndEnRetardTrue(devoirId);

        List<Soumission> soumissions = soumissionRepository.findByDevoirIdOrderByDateSoumissionDesc(devoirId);

        BigDecimal moyenneNotes = BigDecimal.ZERO;
        long notesCount = soumissions.stream().filter(s -> s.getNote() != null).count();
        if (notesCount > 0) {
            BigDecimal sum = soumissions.stream()
                    .filter(s -> s.getNote() != null)
                    .map(Soumission::getNote)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            moyenneNotes = sum.divide(BigDecimal.valueOf(notesCount), 2, RoundingMode.HALF_UP);
        }

        long totalDevoirs = devoirRepository.count();
        double tauxSoumission = totalSoumissions > 0 ? (double) totalSoumissions / totalSoumissions * 100 : 0;

        return DevoirStatsDTO.builder()
                .totalDevoirs(totalDevoirs)
                .totalSoumissions(totalSoumissions)
                .tauxSoumission(tauxSoumission)
                .moyenneNotes(moyenneNotes)
                .enRetard(enRetard)
                .build();
    }

    @Transactional
    public void delete(UUID id) {
        if (!soumissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Soumission", id);
        }
        soumissionRepository.deleteById(id);
    }

    private SoumissionDTO toDTO(Soumission soumission) {
        return SoumissionDTO.builder()
                .id(soumission.getId())
                .devoirId(soumission.getDevoir().getId())
                .devoirTitre(soumission.getDevoir().getTitre())
                .eleveId(soumission.getEleveId())
                .contenu(soumission.getContenu())
                .fichierUrl(soumission.getFichierUrl())
                .dateSoumission(soumission.getDateSoumission())
                .note(soumission.getNote())
                .commentaireCorrection(soumission.getCommentaireCorrection())
                .corrige(soumission.getCorrige())
                .enRetard(soumission.getEnRetard())
                .anneeScolaire(soumission.getAnneeScolaire())
                .createdAt(soumission.getCreatedAt())
                .build();
    }
}
