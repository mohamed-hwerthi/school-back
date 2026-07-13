package com.schoolSys.schooolSys.anneescolaire;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.dto.*;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnneeScolaireService {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final TrimestreRepository trimestreRepository;
    private final VacanceRepository vacanceRepository;
    private final JourFerieRepository jourFerieRepository;

    // --- AnneeScolaire CRUD ---

    @Transactional(readOnly = true)
    public List<AnneeScolaireResponseDTO> getAllAnnees() {
        return anneeScolaireRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AnneeScolaireResponseDTO getAnneeById(UUID id) {
        AnneeScolaire annee = anneeScolaireRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", id));
        return toDto(annee);
    }

    public AnneeScolaireResponseDTO getActiveAnnee() {
        AnneeScolaire annee = anneeScolaireRepository.findByActiveTrue()
            .orElseThrow(() -> new RuntimeException("Aucune annee scolaire active trouvee"));
        return toDto(annee);
    }

    @Transactional
    public AnneeScolaireResponseDTO createAnnee(AnneeScolaireRequestDTO request) {
        AnneeScolaire annee = AnneeScolaire.builder()
            .label(request.getLabel())
            .dateDebut(request.getDateDebut())
            .dateFin(request.getDateFin())
            .active(request.getActive() != null ? request.getActive() : false)
            .build();

        // If this is set as active, deactivate all others
        if (Boolean.TRUE.equals(annee.getActive())) {
            deactivateAllAnnees();
        }

        AnneeScolaire saved = anneeScolaireRepository.save(annee);

        if (request.getTrimestres() != null) {
            request.getTrimestres().forEach(dto -> {
                Trimestre t = Trimestre.builder()
                    .anneeScolaire(saved)
                    .numero(dto.getNumero())
                    .label(dto.getLabel())
                    .dateDebut(dto.getDateDebut())
                    .dateFin(dto.getDateFin())
                    .saisieNotesOuverte(dto.getSaisieNotesOuverte() != null ? dto.getSaisieNotesOuverte() : true)
                    .build();
                saved.getTrimestres().add(t);
            });
        }

        if (request.getVacances() != null) {
            request.getVacances().forEach(dto -> {
                Vacance v = Vacance.builder()
                    .anneeScolaire(saved)
                    .label(dto.getLabel())
                    .dateDebut(dto.getDateDebut())
                    .dateFin(dto.getDateFin())
                    .build();
                saved.getVacances().add(v);
            });
        }

        if (request.getJoursFeries() != null) {
            request.getJoursFeries().forEach(dto -> {
                JourFerie jf = JourFerie.builder()
                    .anneeScolaire(saved)
                    .label(dto.getLabel())
                    .date(dto.getDate())
                    .build();
                saved.getJoursFeries().add(jf);
            });
        }

        return toDto(anneeScolaireRepository.save(saved));
    }

    @Transactional
    public AnneeScolaireResponseDTO updateAnnee(UUID id, AnneeScolaireRequestDTO request) {
        AnneeScolaire annee = anneeScolaireRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", id));
        assertModifiable(annee);

        annee.setLabel(request.getLabel());
        annee.setDateDebut(request.getDateDebut());
        annee.setDateFin(request.getDateFin());

        if (request.getActive() != null) {
            if (Boolean.TRUE.equals(request.getActive()) && !Boolean.TRUE.equals(annee.getActive())) {
                deactivateAllAnnees();
            }
            annee.setActive(request.getActive());
        }

        // Replace trimestres
        annee.getTrimestres().clear();
        if (request.getTrimestres() != null) {
            request.getTrimestres().forEach(dto -> {
                Trimestre t = Trimestre.builder()
                    .anneeScolaire(annee)
                    .numero(dto.getNumero())
                    .label(dto.getLabel())
                    .dateDebut(dto.getDateDebut())
                    .dateFin(dto.getDateFin())
                    .saisieNotesOuverte(dto.getSaisieNotesOuverte() != null ? dto.getSaisieNotesOuverte() : true)
                    .build();
                annee.getTrimestres().add(t);
            });
        }

        // Replace vacances
        annee.getVacances().clear();
        if (request.getVacances() != null) {
            request.getVacances().forEach(dto -> {
                Vacance v = Vacance.builder()
                    .anneeScolaire(annee)
                    .label(dto.getLabel())
                    .dateDebut(dto.getDateDebut())
                    .dateFin(dto.getDateFin())
                    .build();
                annee.getVacances().add(v);
            });
        }

        // Replace jours feries
        annee.getJoursFeries().clear();
        if (request.getJoursFeries() != null) {
            request.getJoursFeries().forEach(dto -> {
                JourFerie jf = JourFerie.builder()
                    .anneeScolaire(annee)
                    .label(dto.getLabel())
                    .date(dto.getDate())
                    .build();
                annee.getJoursFeries().add(jf);
            });
        }

        return toDto(anneeScolaireRepository.save(annee));
    }

    @Transactional
    public void deleteAnnee(UUID id) {
        if (!anneeScolaireRepository.existsById(id)) throw new ResourceNotFoundException("AnneeScolaire", id);
        anneeScolaireRepository.deleteById(id);
    }

    @Transactional
    public AnneeScolaireResponseDTO activateAnnee(UUID id) {
        AnneeScolaire annee = anneeScolaireRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", id));
        assertModifiable(annee);
        deactivateAllAnnees();
        annee.setActive(true);
        return toDto(anneeScolaireRepository.save(annee));
    }

    @Transactional
    public AnneeScolaireResponseDTO cloturerAnnee(UUID id) {
        AnneeScolaire annee = anneeScolaireRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", id));
        annee.setCloturee(true);
        annee.setActive(false);
        return toDto(anneeScolaireRepository.save(annee));
    }

    // --- Trimestre operations ---

    @Transactional(readOnly = true)
    public List<TrimestreDTO> getTrimestresByAnnee(UUID anneeScolaireId) {
        if (!anneeScolaireRepository.existsById(anneeScolaireId))
            throw new ResourceNotFoundException("AnneeScolaire", anneeScolaireId);
        return trimestreRepository.findByAnneeScolaireId(anneeScolaireId).stream()
            .map(this::toTrimestreDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public TrimestreDTO addTrimestre(UUID anneeScolaireId, TrimestreDTO dto) {
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeScolaireId)
            .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", anneeScolaireId));
        assertModifiable(annee);
        Trimestre t = Trimestre.builder()
            .anneeScolaire(annee)
            .numero(dto.getNumero())
            .label(dto.getLabel())
            .dateDebut(dto.getDateDebut())
            .dateFin(dto.getDateFin())
            .saisieNotesOuverte(dto.getSaisieNotesOuverte() != null ? dto.getSaisieNotesOuverte() : true)
            .build();
        return toTrimestreDto(trimestreRepository.save(t));
    }

    @Transactional
    public TrimestreDTO updateTrimestre(UUID id, TrimestreDTO dto) {
        Trimestre t = trimestreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trimestre", id));
        assertModifiable(t.getAnneeScolaire());
        t.setNumero(dto.getNumero());
        t.setLabel(dto.getLabel());
        t.setDateDebut(dto.getDateDebut());
        t.setDateFin(dto.getDateFin());
        if (dto.getSaisieNotesOuverte() != null) {
            t.setSaisieNotesOuverte(dto.getSaisieNotesOuverte());
        }
        return toTrimestreDto(trimestreRepository.save(t));
    }

    @Transactional
    public void deleteTrimestre(UUID id) {
        Trimestre t = trimestreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trimestre", id));
        assertModifiable(t.getAnneeScolaire());
        trimestreRepository.deleteById(id);
    }

    // --- Vacance operations ---

    @Transactional(readOnly = true)
    public List<VacanceDTO> getVacancesByAnnee(UUID anneeScolaireId) {
        if (!anneeScolaireRepository.existsById(anneeScolaireId))
            throw new ResourceNotFoundException("AnneeScolaire", anneeScolaireId);
        return vacanceRepository.findByAnneeScolaireId(anneeScolaireId).stream()
            .map(this::toVacanceDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public VacanceDTO addVacance(UUID anneeScolaireId, VacanceDTO dto) {
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeScolaireId)
            .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", anneeScolaireId));
        Vacance v = Vacance.builder()
            .anneeScolaire(annee)
            .label(dto.getLabel())
            .dateDebut(dto.getDateDebut())
            .dateFin(dto.getDateFin())
            .build();
        return toVacanceDto(vacanceRepository.save(v));
    }

    @Transactional
    public VacanceDTO updateVacance(UUID id, VacanceDTO dto) {
        Vacance v = vacanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vacance", id));
        v.setLabel(dto.getLabel());
        v.setDateDebut(dto.getDateDebut());
        v.setDateFin(dto.getDateFin());
        return toVacanceDto(vacanceRepository.save(v));
    }

    @Transactional
    public void deleteVacance(UUID id) {
        if (!vacanceRepository.existsById(id)) throw new ResourceNotFoundException("Vacance", id);
        vacanceRepository.deleteById(id);
    }

    // --- JourFerie operations ---

    @Transactional(readOnly = true)
    public List<JourFerieDTO> getJoursFeriesByAnnee(UUID anneeScolaireId) {
        if (!anneeScolaireRepository.existsById(anneeScolaireId))
            throw new ResourceNotFoundException("AnneeScolaire", anneeScolaireId);
        return jourFerieRepository.findByAnneeScolaireId(anneeScolaireId).stream()
            .map(this::toJourFerieDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public JourFerieDTO addJourFerie(UUID anneeScolaireId, JourFerieDTO dto) {
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeScolaireId)
            .orElseThrow(() -> new ResourceNotFoundException("AnneeScolaire", anneeScolaireId));
        JourFerie jf = JourFerie.builder()
            .anneeScolaire(annee)
            .label(dto.getLabel())
            .date(dto.getDate())
            .build();
        return toJourFerieDto(jourFerieRepository.save(jf));
    }

    @Transactional
    public JourFerieDTO updateJourFerie(UUID id, JourFerieDTO dto) {
        JourFerie jf = jourFerieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("JourFerie", id));
        jf.setLabel(dto.getLabel());
        jf.setDate(dto.getDate());
        return toJourFerieDto(jourFerieRepository.save(jf));
    }

    @Transactional
    public void deleteJourFerie(UUID id) {
        if (!jourFerieRepository.existsById(id)) throw new ResourceNotFoundException("JourFerie", id);
        jourFerieRepository.deleteById(id);
    }

    // --- Helpers ---

    private void deactivateAllAnnees() {
        anneeScolaireRepository.findAll().forEach(a -> {
            if (Boolean.TRUE.equals(a.getActive())) {
                a.setActive(false);
                anneeScolaireRepository.save(a);
            }
        });
    }

    /** ANN-032: a clôturée year is read-only — its structure cannot be modified. */
    private void assertModifiable(AnneeScolaire annee) {
        if (annee != null && Boolean.TRUE.equals(annee.getCloturee())) {
            throw new IllegalStateException("Année scolaire « " + annee.getLabel()
                    + " » clôturée — modification impossible (lecture seule).");
        }
    }

    // --- Mappers ---

    private AnneeScolaireResponseDTO toDto(AnneeScolaire a) {
        List<TrimestreDTO> trimestres = a.getTrimestres().stream()
            .map(this::toTrimestreDto)
            .collect(Collectors.toList());

        List<VacanceDTO> vacances = a.getVacances().stream()
            .map(this::toVacanceDto)
            .collect(Collectors.toList());

        List<JourFerieDTO> joursFeries = a.getJoursFeries().stream()
            .map(this::toJourFerieDto)
            .collect(Collectors.toList());

        return AnneeScolaireResponseDTO.builder()
            .id(a.getId())
            .label(a.getLabel())
            .dateDebut(a.getDateDebut())
            .dateFin(a.getDateFin())
            .active(a.getActive())
            .cloturee(a.getCloturee())
            .trimestres(trimestres)
            .vacances(vacances)
            .joursFeries(joursFeries)
            .createdAt(a.getCreatedAt())
            .build();
    }

    private TrimestreDTO toTrimestreDto(Trimestre t) {
        return TrimestreDTO.builder()
            .id(t.getId())
            .numero(t.getNumero())
            .label(t.getLabel())
            .dateDebut(t.getDateDebut())
            .dateFin(t.getDateFin())
            .saisieNotesOuverte(t.getSaisieNotesOuverte())
            .build();
    }

    private VacanceDTO toVacanceDto(Vacance v) {
        return VacanceDTO.builder()
            .id(v.getId())
            .label(v.getLabel())
            .dateDebut(v.getDateDebut())
            .dateFin(v.getDateFin())
            .build();
    }

    private JourFerieDTO toJourFerieDto(JourFerie jf) {
        return JourFerieDTO.builder()
            .id(jf.getId())
            .label(jf.getLabel())
            .date(jf.getDate())
            .build();
    }
}
