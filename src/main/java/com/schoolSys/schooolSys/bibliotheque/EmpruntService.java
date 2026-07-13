package com.schoolSys.schooolSys.bibliotheque;

import java.util.UUID;

import com.schoolSys.schooolSys.bibliotheque.dto.BibliothequeStatsDTO;
import com.schoolSys.schooolSys.bibliotheque.dto.CreateEmpruntRequest;
import com.schoolSys.schooolSys.bibliotheque.dto.EmpruntDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final LivreRepository livreRepository;
    private final StudentRepository studentRepository;
    private final CurrentUserContext currentUserContext;

    public List<EmpruntDTO> findAll() {
        return empruntRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public Page<EmpruntDTO> findAll(Pageable pageable) {
        return empruntRepository.findAll(pageable)
                .map(this::toDTO);
    }

    public EmpruntDTO findById(UUID id) {
        Emprunt emprunt = empruntRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt", id));
        return toDTO(emprunt);
    }

    public List<EmpruntDTO> findByEleve(UUID eleveId) {
        currentUserContext.assertCanAccessStudent(eleveId);
        return empruntRepository.findByEleveId(eleveId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EmpruntDTO> findByLivre(UUID livreId) {
        return empruntRepository.findByLivreId(livreId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EmpruntDTO> findEnRetard() {
        // Find emprunts that are EN_COURS and past the return date
        List<Emprunt> overdue = empruntRepository
                .findByDateRetourPrevueBeforeAndStatut(LocalDate.now(), Emprunt.StatutEmprunt.EN_COURS);
        // Also include those already marked as EN_RETARD
        List<Emprunt> enRetard = empruntRepository.findByStatut(Emprunt.StatutEmprunt.EN_RETARD);

        // Merge, avoiding duplicates by id
        java.util.Map<UUID, Emprunt> map = new java.util.LinkedHashMap<>();
        overdue.forEach(e -> map.put(e.getId(), e));
        enRetard.forEach(e -> map.put(e.getId(), e));

        return map.values().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public EmpruntDTO create(CreateEmpruntRequest request) {
        Livre livre = livreRepository.findById(request.getLivreId())
                .orElseThrow(() -> new ResourceNotFoundException("Livre", request.getLivreId()));

        if (livre.getExemplairesDisponibles() == null || livre.getExemplairesDisponibles() <= 0) {
            throw new IllegalStateException("Aucun exemplaire disponible pour ce livre");
        }

        // Verify student exists
        if (!studentRepository.existsById(request.getEleveId())) {
            throw new ResourceNotFoundException("Student", request.getEleveId());
        }

        // Decrement available copies
        livre.setExemplairesDisponibles(livre.getExemplairesDisponibles() - 1);
        livreRepository.save(livre);

        Emprunt emprunt = Emprunt.builder()
                .livre(livre)
                .eleveId(request.getEleveId())
                .dateEmprunt(LocalDate.now())
                .dateRetourPrevue(request.getDateRetourPrevue())
                .statut(Emprunt.StatutEmprunt.EN_COURS)
                .notes(request.getNotes())
                .build();

        return toDTO(empruntRepository.save(emprunt));
    }

    @Transactional
    public EmpruntDTO retourner(UUID empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt", empruntId));

        if (emprunt.getStatut() == Emprunt.StatutEmprunt.RETOURNE) {
            throw new IllegalStateException("Cet emprunt est deja retourne");
        }

        emprunt.setDateRetourEffective(LocalDate.now());
        emprunt.setStatut(Emprunt.StatutEmprunt.RETOURNE);

        // Increment available copies
        Livre livre = emprunt.getLivre();
        livre.setExemplairesDisponibles(
                (livre.getExemplairesDisponibles() != null ? livre.getExemplairesDisponibles() : 0) + 1);
        livreRepository.save(livre);

        return toDTO(empruntRepository.save(emprunt));
    }

    public BibliothequeStatsDTO getStats() {
        long totalLivres = livreRepository.count();
        long totalEmprunts = empruntRepository.count();
        long empruntsEnCours = empruntRepository.countByStatut(Emprunt.StatutEmprunt.EN_COURS);
        long empruntsEnRetard = empruntRepository.countByStatut(Emprunt.StatutEmprunt.EN_RETARD);

        // Also count EN_COURS emprunts that are past due date
        long overdueButNotMarked = empruntRepository
                .findByDateRetourPrevueBeforeAndStatut(LocalDate.now(), Emprunt.StatutEmprunt.EN_COURS)
                .size();
        empruntsEnRetard += overdueButNotMarked;

        // Top 10 most borrowed books
        List<Object[]> topBooks = empruntRepository.findLivresLesPlusEmpruntes(PageRequest.of(0, 10));
        List<BibliothequeStatsDTO.LivreEmprunteDTO> topList = topBooks.stream()
                .map(row -> BibliothequeStatsDTO.LivreEmprunteDTO.builder()
                        .livreId((UUID) row[0])
                        .titre((String) row[1])
                        .count((Long) row[2])
                        .build())
                .toList();

        return BibliothequeStatsDTO.builder()
                .totalLivres(totalLivres)
                .totalEmprunts(totalEmprunts)
                .empruntsEnCours(empruntsEnCours)
                .empruntsEnRetard(empruntsEnRetard)
                .livresLesPlusEmpruntes(topList)
                .build();
    }

    // ─── Mapping ────────────────────────────────────────────

    private EmpruntDTO toDTO(Emprunt emprunt) {
        String eleveName = "";
        try {
            Student student = studentRepository.findById(emprunt.getEleveId()).orElse(null);
            if (student != null) {
                eleveName = student.getFirstName() + " " + student.getLastName();
            }
        } catch (Exception ignored) {
            // Student might not exist
        }

        return EmpruntDTO.builder()
                .id(emprunt.getId())
                .livreId(emprunt.getLivre().getId())
                .livreTitle(emprunt.getLivre().getTitre())
                .eleveId(emprunt.getEleveId())
                .eleveName(eleveName)
                .dateEmprunt(emprunt.getDateEmprunt())
                .dateRetourPrevue(emprunt.getDateRetourPrevue())
                .dateRetourEffective(emprunt.getDateRetourEffective())
                .statut(emprunt.getStatut())
                .penalite(emprunt.getPenalite())
                .notes(emprunt.getNotes())
                .createdAt(emprunt.getCreatedAt())
                .updatedAt(emprunt.getUpdatedAt())
                .build();
    }
}
