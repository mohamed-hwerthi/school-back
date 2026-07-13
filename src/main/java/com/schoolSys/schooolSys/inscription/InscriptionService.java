package com.schoolSys.schooolSys.inscription;

import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.inscription.dto.*;
import com.schoolSys.schooolSys.niveau.Niveau;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import com.schoolSys.schooolSys.student.StudentService;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final ListeAttenteRepository listeAttenteRepository;
    private final NiveauRepository niveauRepository;
    private final StudentService studentService;

    /**
     * Create a new inscription (public - no auth required).
     */
    @Transactional
    public InscriptionDTO create(CreateInscriptionRequest request) {
        Inscription inscription = Inscription.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .dateNaissance(request.getDateNaissance())
                .lieuNaissance(request.getLieuNaissance())
                .sexe(request.getSexe())
                .adresse(request.getAdresse())
                .telephoneParent(request.getTelephoneParent())
                .emailParent(request.getEmailParent())
                .nomParent(request.getNomParent())
                .prenomParent(request.getPrenomParent())
                .niveauId(request.getNiveauId())
                .anneeScolaire(request.getAnneeScolaire())
                .statut("SOUMISE")
                .numeroDossier(generateNumeroDossier())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Inscription saved = inscriptionRepository.save(inscription);
        return toDTO(saved);
    }

    /**
     * List inscriptions with optional filters.
     */
    public PagedResponse<InscriptionDTO> findAll(String statut, String anneeScolaire, UUID niveauId, Pageable pageable) {
        Page<Inscription> page;

        if (statut != null && anneeScolaire != null && niveauId != null) {
            page = inscriptionRepository.findByStatutAndAnneeScolaireAndNiveauId(statut, anneeScolaire, niveauId, pageable);
        } else if (statut != null && anneeScolaire != null) {
            page = inscriptionRepository.findByStatutAndAnneeScolaire(statut, anneeScolaire, pageable);
        } else if (statut != null && niveauId != null) {
            page = inscriptionRepository.findByStatutAndNiveauId(statut, niveauId, pageable);
        } else if (anneeScolaire != null && niveauId != null) {
            page = inscriptionRepository.findByAnneeScolaireAndNiveauId(anneeScolaire, niveauId, pageable);
        } else if (statut != null) {
            page = inscriptionRepository.findByStatut(statut, pageable);
        } else if (anneeScolaire != null) {
            page = inscriptionRepository.findByAnneeScolaire(anneeScolaire, pageable);
        } else if (niveauId != null) {
            page = inscriptionRepository.findByNiveauId(niveauId, pageable);
        } else {
            page = inscriptionRepository.findAll(pageable);
        }

        List<InscriptionDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .toList();

        return PagedResponse.from(page, content);
    }

    /**
     * Get a single inscription by ID.
     */
    public InscriptionDTO findById(UUID id) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", id));
        return toDTO(inscription);
    }

    /**
     * Find inscription by numero dossier (public - for parents to check status).
     */
    public InscriptionDTO findByNumeroDossier(String numeroDossier) {
        Inscription inscription = inscriptionRepository.findByNumeroDossier(numeroDossier)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription non trouvee avec le numero de dossier: " + numeroDossier));
        return toDTO(inscription);
    }

    /**
     * Update inscription status.
     */
    @Transactional
    public InscriptionDTO updateStatut(UUID id, UpdateStatutRequest request) {
        Inscription inscription = inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", id));

        inscription.setStatut(request.getStatut());
        if (request.getCommentaire() != null) {
            inscription.setCommentaire(request.getCommentaire());
        }

        // If status is LISTE_ATTENTE, automatically add to waiting list
        if ("LISTE_ATTENTE".equals(request.getStatut()) && inscription.getNiveauId() != null) {
            addToListeAttenteInternal(inscription);
        }

        Inscription updated = inscriptionRepository.save(inscription);
        return toDTO(updated);
    }

    /**
     * Get inscription statistics.
     */
    public InscriptionStatsDTO getStats(String anneeScolaire) {
        long totalSoumises;
        long totalAcceptees;
        long totalRefusees;
        long totalEnAttente;
        long totalListeAttente;

        if (anneeScolaire != null) {
            totalSoumises = inscriptionRepository.countByAnneeScolaireAndStatut(anneeScolaire, "SOUMISE");
            totalAcceptees = inscriptionRepository.countByAnneeScolaireAndStatut(anneeScolaire, "ACCEPTEE");
            totalRefusees = inscriptionRepository.countByAnneeScolaireAndStatut(anneeScolaire, "REFUSEE");
            totalEnAttente = inscriptionRepository.countByAnneeScolaireAndStatut(anneeScolaire, "EN_ATTENTE");
            totalListeAttente = inscriptionRepository.countByAnneeScolaireAndStatut(anneeScolaire, "LISTE_ATTENTE");
        } else {
            totalSoumises = inscriptionRepository.countByStatut("SOUMISE");
            totalAcceptees = inscriptionRepository.countByStatut("ACCEPTEE");
            totalRefusees = inscriptionRepository.countByStatut("REFUSEE");
            totalEnAttente = inscriptionRepository.countByStatut("EN_ATTENTE");
            totalListeAttente = inscriptionRepository.countByStatut("LISTE_ATTENTE");
        }

        long total = totalSoumises + totalAcceptees + totalRefusees + totalEnAttente + totalListeAttente;
        double tauxConversion = total > 0 ? (double) totalAcceptees / total * 100 : 0;

        return InscriptionStatsDTO.builder()
                .totalSoumises(totalSoumises)
                .totalAcceptees(totalAcceptees)
                .totalRefusees(totalRefusees)
                .totalEnAttente(totalEnAttente)
                .totalListeAttente(totalListeAttente)
                .tauxConversion(Math.round(tauxConversion * 100.0) / 100.0)
                .build();
    }

    /**
     * Get waiting list for a specific niveau.
     */
    public List<InscriptionDTO> getListeAttente(UUID niveauId, String anneeScolaire) {
        String annee = anneeScolaire != null ? anneeScolaire : currentAnneeScolaire();
        List<ListeAttente> entries = listeAttenteRepository
                .findByNiveauIdAndAnneeScolaireOrderByPosition(niveauId, annee);

        return entries.stream()
                .map(la -> {
                    Inscription inscription = inscriptionRepository.findById(la.getInscriptionId())
                            .orElse(null);
                    if (inscription == null) return null;
                    InscriptionDTO dto = toDTO(inscription);
                    return dto;
                })
                .filter(dto -> dto != null)
                .toList();
    }

    /**
     * Add inscription to waiting list.
     */
    @Transactional
    public void addToListeAttente(UUID inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", inscriptionId));

        if (inscription.getNiveauId() == null) {
            throw new IllegalArgumentException("L'inscription doit avoir un niveau pour etre ajoutee a la liste d'attente");
        }

        inscription.setStatut("LISTE_ATTENTE");
        inscriptionRepository.save(inscription);
        addToListeAttenteInternal(inscription);
    }

    /**
     * Convert an accepted inscription into a student.
     */
    @Transactional
    public StudentResponseDTO convertToStudent(UUID inscriptionId, String classe, String sexe) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscription", inscriptionId));

        if (!"ACCEPTEE".equals(inscription.getStatut())) {
            throw new IllegalStateException("Seules les inscriptions acceptees peuvent etre converties en eleves");
        }

        String resolvedSexe = sexe != null ? sexe : inscription.getSexe();
        if (resolvedSexe == null || resolvedSexe.isBlank()) {
            throw new IllegalArgumentException("Le champ « sexe » est obligatoire pour convertir en élève. Veuillez sélectionner le genre.");
        }

        String niveauNom = null;
        if (inscription.getNiveauId() != null) {
            niveauNom = niveauRepository.findById(inscription.getNiveauId())
                    .map(Niveau::getName)
                    .orElse(null);
        }

        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName(inscription.getPrenom());
        dto.setLastName(inscription.getNom());
        dto.setSex(resolvedSexe);
        dto.setDateOfBirth(inscription.getDateNaissance());
        dto.setBirthPlace(inscription.getLieuNaissance());
        dto.setAddress(inscription.getAdresse());
        dto.setParentFirstName(inscription.getPrenomParent());
        dto.setParentLastName(inscription.getNomParent());
        dto.setParentPhone(inscription.getTelephoneParent());
        dto.setParentEmail(inscription.getEmailParent());
        dto.setNiveau(niveauNom);
        dto.setClasse(classe);
        dto.setEnrollmentDate(LocalDate.now());
        dto.setStatus("Actif");
        dto.setIsBlocked(false);

        return studentService.create(dto);
    }

    // ── Private helpers ────────────────────────────────────────────

    private void addToListeAttenteInternal(Inscription inscription) {
        // Check if already on waiting list
        if (listeAttenteRepository.findByInscriptionId(inscription.getId()).isPresent()) {
            return;
        }

        int maxPosition = listeAttenteRepository.findMaxPositionByNiveauIdAndAnneeScolaire(
                inscription.getNiveauId(), inscription.getAnneeScolaire());

        ListeAttente entry = ListeAttente.builder()
                .inscriptionId(inscription.getId())
                .niveauId(inscription.getNiveauId())
                .position(maxPosition + 1)
                .anneeScolaire(inscription.getAnneeScolaire())
                .createdAt(LocalDateTime.now())
                .build();

        listeAttenteRepository.save(entry);
    }

    private String generateNumeroDossier() {
        int year = Year.now().getValue();
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("INS-%d-%s", year, uuid);
    }

    private String currentAnneeScolaire() {
        int year = Year.now().getValue();
        int month = java.time.LocalDate.now().getMonthValue();
        if (month >= 9) {
            return year + "-" + (year + 1);
        }
        return (year - 1) + "-" + year;
    }

    private InscriptionDTO toDTO(Inscription inscription) {
        String niveauNom = null;
        if (inscription.getNiveauId() != null) {
            niveauNom = niveauRepository.findById(inscription.getNiveauId())
                    .map(Niveau::getName)
                    .orElse(null);
        }

        return InscriptionDTO.builder()
                .id(inscription.getId())
                .nom(inscription.getNom())
                .prenom(inscription.getPrenom())
                .dateNaissance(inscription.getDateNaissance())
                .lieuNaissance(inscription.getLieuNaissance())
                .sexe(inscription.getSexe())
                .adresse(inscription.getAdresse())
                .telephoneParent(inscription.getTelephoneParent())
                .emailParent(inscription.getEmailParent())
                .nomParent(inscription.getNomParent())
                .prenomParent(inscription.getPrenomParent())
                .niveauId(inscription.getNiveauId())
                .niveauNom(niveauNom)
                .anneeScolaire(inscription.getAnneeScolaire())
                .statut(inscription.getStatut())
                .commentaire(inscription.getCommentaire())
                .numeroDossier(inscription.getNumeroDossier())
                .documentsPaths(inscription.getDocumentsPaths())
                .montantFrais(inscription.getMontantFrais())
                .fraisPaye(inscription.getFraisPaye())
                .createdAt(inscription.getCreatedAt())
                .updatedAt(inscription.getUpdatedAt())
                .build();
    }
}
