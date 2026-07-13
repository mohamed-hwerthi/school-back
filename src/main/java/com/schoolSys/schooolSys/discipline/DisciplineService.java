package com.schoolSys.schooolSys.discipline;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.discipline.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DisciplineService {

    private final IncidentRepository incidentRepository;
    private final SanctionRepository sanctionRepository;
    private final DisciplineNotificationService notificationService;
    private final CurrentUserContext currentUser;

    // --- Row-level scoping helpers (a teacher only sees his own students) ---

    /** Filters incidents to those involving at least one student in the teacher's classes. */
    private List<Incident> scopeIncidents(List<Incident> incidents) {
        if (!currentUser.hasRole(UserRole.ENSEIGNANT)) return incidents;
        Set<UUID> scoped = currentUser.getScopedStudentIdsForTeacher();
        return incidents.stream()
            .filter(i -> i.getElevesImpliques().stream()
                .anyMatch(ie -> scoped.contains(ie.getEleveId())))
            .collect(Collectors.toList());
    }

    /** Filters sanctions to those concerning a student in the teacher's classes. */
    private List<Sanction> scopeSanctions(List<Sanction> sanctions) {
        if (!currentUser.hasRole(UserRole.ENSEIGNANT)) return sanctions;
        Set<UUID> scoped = currentUser.getScopedStudentIdsForTeacher();
        return sanctions.stream()
            .filter(s -> scoped.contains(s.getEleveId()))
            .collect(Collectors.toList());
    }

    private void assertCanAccessIncident(Incident incident) {
        if (!currentUser.hasRole(UserRole.ENSEIGNANT)) return;
        Set<UUID> scoped = currentUser.getScopedStudentIdsForTeacher();
        boolean inScope = incident.getElevesImpliques().stream()
            .anyMatch(ie -> scoped.contains(ie.getEleveId()));
        if (!inScope) {
            throw new AccessDeniedException("Cet incident n'est pas dans votre périmètre.");
        }
    }

    // --- Incident CRUD ---

    public List<IncidentResponseDTO> getAllIncidents() {
        return scopeIncidents(incidentRepository.findAll()).stream()
            .map(this::toIncidentDto)
            .collect(Collectors.toList());
    }

    public IncidentResponseDTO getIncidentById(UUID id) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
        assertCanAccessIncident(incident);
        return toIncidentDto(incident);
    }

    public List<IncidentResponseDTO> getIncidentsByDateRange(LocalDate start, LocalDate end) {
        return scopeIncidents(incidentRepository.findByDateBetween(start, end)).stream()
            .map(this::toIncidentDto)
            .collect(Collectors.toList());
    }

    public List<IncidentResponseDTO> getIncidentsByType(String type) {
        return scopeIncidents(incidentRepository.findByType(type)).stream()
            .map(this::toIncidentDto)
            .collect(Collectors.toList());
    }

    public List<IncidentResponseDTO> getIncidentsByGravite(String gravite) {
        return scopeIncidents(incidentRepository.findByGravite(gravite)).stream()
            .map(this::toIncidentDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public IncidentResponseDTO createIncident(IncidentRequestDTO request) {
        Incident incident = Incident.builder()
            .titre(request.getTitre())
            .description(request.getDescription())
            .date(request.getDate())
            .type(request.getType())
            .gravite(request.getGravite())
            .lieu(request.getLieu())
            .signaleParId(request.getSignaleParId())
            .build();

        if (request.getElevesImpliques() != null) {
            List<IncidentEleve> eleves = request.getElevesImpliques().stream()
                .map(dto -> IncidentEleve.builder()
                    .incident(incident)
                    .eleveId(dto.getEleveId())
                    .roleEleve(dto.getRoleEleve())
                    .build())
                .collect(Collectors.toList());
            incident.getElevesImpliques().addAll(eleves);
        }

        Incident saved = incidentRepository.save(incident);

        // DISC-006: Notify parents for each student involved
        if (request.getElevesImpliques() != null) {
            for (IncidentRequestDTO.IncidentEleveDTO eleveDto : request.getElevesImpliques()) {
                notificationService.notifyParentIncident(saved, eleveDto.getEleveId());
            }
        }

        return toIncidentDto(saved);
    }

    @Transactional
    public IncidentResponseDTO updateIncident(UUID id, IncidentRequestDTO request) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Incident", id));

        incident.setTitre(request.getTitre());
        incident.setDescription(request.getDescription());
        incident.setDate(request.getDate());
        incident.setType(request.getType());
        incident.setGravite(request.getGravite());
        incident.setLieu(request.getLieu());
        incident.setSignaleParId(request.getSignaleParId());

        incident.getElevesImpliques().clear();
        if (request.getElevesImpliques() != null) {
            List<IncidentEleve> eleves = request.getElevesImpliques().stream()
                .map(dto -> IncidentEleve.builder()
                    .incident(incident)
                    .eleveId(dto.getEleveId())
                    .roleEleve(dto.getRoleEleve())
                    .build())
                .collect(Collectors.toList());
            incident.getElevesImpliques().addAll(eleves);
        }

        return toIncidentDto(incidentRepository.save(incident));
    }

    @Transactional
    public void deleteIncident(UUID id) {
        if (!incidentRepository.existsById(id)) throw new ResourceNotFoundException("Incident", id);
        incidentRepository.deleteById(id);
    }

    // --- Sanction CRUD ---

    public List<SanctionResponseDTO> getAllSanctions() {
        return scopeSanctions(sanctionRepository.findAll()).stream()
            .map(this::toSanctionDto)
            .collect(Collectors.toList());
    }

    public SanctionResponseDTO getSanctionById(UUID id) {
        Sanction sanction = sanctionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sanction", id));
        currentUser.assertCanAccessStudent(sanction.getEleveId());
        return toSanctionDto(sanction);
    }

    public List<SanctionResponseDTO> getSanctionsByEleve(UUID eleveId) {
        currentUser.assertCanAccessStudent(eleveId);
        return sanctionRepository.findByEleveId(eleveId).stream()
            .map(this::toSanctionDto)
            .collect(Collectors.toList());
    }

    public List<SanctionResponseDTO> getSanctionsByIncident(UUID incidentId) {
        return scopeSanctions(sanctionRepository.findByIncidentId(incidentId)).stream()
            .map(this::toSanctionDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public SanctionResponseDTO createSanction(SanctionRequestDTO request) {
        Incident incident = null;
        if (request.getIncidentId() != null) {
            incident = incidentRepository.findById(request.getIncidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Incident", request.getIncidentId()));
        }

        // Determine niveau from type if not explicitly provided
        int niveau = request.getNiveau() != null ? request.getNiveau() : typeToNiveau(request.getType());

        Sanction sanction = Sanction.builder()
            .eleveId(request.getEleveId())
            .incident(incident)
            .type(request.getType())
            .description(request.getDescription())
            .dateDebut(request.getDateDebut())
            .dateFin(request.getDateFin())
            .decideParId(request.getDecideParId())
            .notifieParents(request.getNotifieParents() != null ? request.getNotifieParents() : false)
            .niveau(niveau)
            .statut(request.getStatut() != null ? request.getStatut() : "ACTIVE")
            .build();

        Sanction saved = sanctionRepository.save(sanction);

        // DISC-006: Notify parent about new sanction
        notificationService.notifyParentSanction(saved);

        return toSanctionDto(saved);
    }

    @Transactional
    public SanctionResponseDTO updateSanction(UUID id, SanctionRequestDTO request) {
        Sanction sanction = sanctionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sanction", id));

        sanction.setEleveId(request.getEleveId());
        sanction.setType(request.getType());
        sanction.setDescription(request.getDescription());
        sanction.setDateDebut(request.getDateDebut());
        sanction.setDateFin(request.getDateFin());
        sanction.setDecideParId(request.getDecideParId());
        sanction.setNotifieParents(request.getNotifieParents() != null ? request.getNotifieParents() : sanction.getNotifieParents());

        if (request.getIncidentId() != null) {
            Incident incident = incidentRepository.findById(request.getIncidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Incident", request.getIncidentId()));
            sanction.setIncident(incident);
        } else {
            sanction.setIncident(null);
        }

        return toSanctionDto(sanctionRepository.save(sanction));
    }

    @Transactional
    public void deleteSanction(UUID id) {
        if (!sanctionRepository.existsById(id)) throw new ResourceNotFoundException("Sanction", id);
        sanctionRepository.deleteById(id);
    }

    // --- DISC-004: Workflow sanction ---

    /**
     * Suggests the next sanction level for a student based on existing active sanctions.
     * 0 sanctions -> AVERTISSEMENT (1), has AVERTISSEMENT -> BLAME (2),
     * has BLAME -> EXCLUSION_TEMPORAIRE (3), has EXCLUSION_TEMPORAIRE -> EXCLUSION_DEFINITIVE (4)
     */
    public Map<String, Object> escalateSanction(UUID eleveId) {
        List<Sanction> activeSanctions = sanctionRepository.findByEleveIdAndStatut(eleveId, "ACTIVE");

        int maxNiveau = activeSanctions.stream()
            .mapToInt(s -> s.getNiveau() != null ? s.getNiveau() : 1)
            .max()
            .orElse(0);

        int suggestedNiveau = Math.min(maxNiveau + 1, 4);
        String suggestedType = niveauToType(suggestedNiveau);
        boolean requiresApproval = suggestedNiveau >= 3; // exclusions need director approval

        Map<String, Object> suggestion = new LinkedHashMap<>();
        suggestion.put("eleveId", eleveId);
        suggestion.put("currentMaxNiveau", maxNiveau);
        suggestion.put("suggestedNiveau", suggestedNiveau);
        suggestion.put("suggestedType", suggestedType);
        suggestion.put("requiresApproval", requiresApproval);
        suggestion.put("activeSanctionsCount", activeSanctions.size());
        return suggestion;
    }

    /**
     * Approve an exclusion sanction (requires director/admin approval).
     */
    @Transactional
    public SanctionResponseDTO approveSanction(UUID sanctionId, UUID approvedByUserId, String comment) {
        Sanction sanction = sanctionRepository.findById(sanctionId)
            .orElseThrow(() -> new ResourceNotFoundException("Sanction", sanctionId));

        sanction.setApprouvePar(approvedByUserId);
        sanction.setCommentaireApprobation(comment);
        sanction.setStatut("ACTIVE");

        return toSanctionDto(sanctionRepository.save(sanction));
    }

    /**
     * Lift (lever) an active sanction.
     */
    @Transactional
    public SanctionResponseDTO leverSanction(UUID sanctionId) {
        Sanction sanction = sanctionRepository.findById(sanctionId)
            .orElseThrow(() -> new ResourceNotFoundException("Sanction", sanctionId));

        sanction.setStatut("LEVEE");

        return toSanctionDto(sanctionRepository.save(sanction));
    }

    /**
     * DISC-005: Get full discipline record for a student (incidents + sanctions chronologically).
     */
    public DossierDisciplinaireDTO getDossierDisciplinaire(UUID eleveId) {
        currentUser.assertCanAccessStudent(eleveId);
        List<Incident> incidents = incidentRepository.findByEleveId(eleveId);
        List<Sanction> sanctions = sanctionRepository.findByEleveIdOrderByCreatedAtDesc(eleveId);

        // Build timeline events
        List<DossierDisciplinaireDTO.EvenementDisciplinaireDTO> timeline = new ArrayList<>();

        for (Incident incident : incidents) {
            timeline.add(DossierDisciplinaireDTO.EvenementDisciplinaireDTO.builder()
                .date(incident.getCreatedAt())
                .type("INCIDENT")
                .description(incident.getTitre() + (incident.getDescription() != null ? " - " + incident.getDescription() : ""))
                .gravite(incident.getGravite())
                .niveau(null)
                .statut(null)
                .id(incident.getId())
                .build());
        }

        for (Sanction sanction : sanctions) {
            timeline.add(DossierDisciplinaireDTO.EvenementDisciplinaireDTO.builder()
                .date(sanction.getCreatedAt())
                .type("SANCTION")
                .description(sanction.getType() + (sanction.getDescription() != null ? " - " + sanction.getDescription() : ""))
                .gravite(null)
                .niveau(sanction.getNiveau())
                .statut(sanction.getStatut())
                .id(sanction.getId())
                .build());
        }

        // Sort timeline by date descending
        timeline.sort(Comparator.comparing(DossierDisciplinaireDTO.EvenementDisciplinaireDTO::getDate).reversed());

        // Calculate current highest active sanction level
        int niveauActuel = sanctions.stream()
            .filter(s -> "ACTIVE".equals(s.getStatut()))
            .mapToInt(s -> s.getNiveau() != null ? s.getNiveau() : 1)
            .max()
            .orElse(0);

        return DossierDisciplinaireDTO.builder()
            .eleveId(eleveId)
            .eleveNom(null) // Would need student service to resolve name
            .totalIncidents(incidents.size())
            .totalSanctions(sanctions.size())
            .niveauActuel(niveauActuel)
            .timeline(timeline)
            .build();
    }

    // --- Helper methods ---

    private int typeToNiveau(String type) {
        if (type == null) return 1;
        switch (type) {
            case "AVERTISSEMENT": return 1;
            case "BLAME": return 2;
            case "EXCLUSION_TEMPORAIRE": return 3;
            case "EXCLUSION_DEFINITIVE": return 4;
            default: return 1;
        }
    }

    private String niveauToType(int niveau) {
        switch (niveau) {
            case 1: return "AVERTISSEMENT";
            case 2: return "BLAME";
            case 3: return "EXCLUSION_TEMPORAIRE";
            case 4: return "EXCLUSION_DEFINITIVE";
            default: return "AVERTISSEMENT";
        }
    }

    // --- Mappers ---

    private IncidentResponseDTO toIncidentDto(Incident i) {
        List<IncidentResponseDTO.IncidentEleveResponseDTO> eleves = i.getElevesImpliques().stream()
            .map(ie -> IncidentResponseDTO.IncidentEleveResponseDTO.builder()
                .id(ie.getId())
                .eleveId(ie.getEleveId())
                .roleEleve(ie.getRoleEleve())
                .build())
            .collect(Collectors.toList());

        return IncidentResponseDTO.builder()
            .id(i.getId())
            .titre(i.getTitre())
            .description(i.getDescription())
            .date(i.getDate())
            .type(i.getType())
            .gravite(i.getGravite())
            .lieu(i.getLieu())
            .signaleParId(i.getSignaleParId())
            .elevesImpliques(eleves)
            .createdAt(i.getCreatedAt())
            .updatedAt(i.getUpdatedAt())
            .build();
    }

    private SanctionResponseDTO toSanctionDto(Sanction s) {
        return SanctionResponseDTO.builder()
            .id(s.getId())
            .eleveId(s.getEleveId())
            .incidentId(s.getIncident() != null ? s.getIncident().getId() : null)
            .type(s.getType())
            .description(s.getDescription())
            .dateDebut(s.getDateDebut())
            .dateFin(s.getDateFin())
            .decideParId(s.getDecideParId())
            .notifieParents(s.getNotifieParents())
            .niveau(s.getNiveau())
            .statut(s.getStatut())
            .approuvePar(s.getApprouvePar())
            .commentaireApprobation(s.getCommentaireApprobation())
            .createdAt(s.getCreatedAt())
            .updatedAt(s.getUpdatedAt())
            .build();
    }
}
