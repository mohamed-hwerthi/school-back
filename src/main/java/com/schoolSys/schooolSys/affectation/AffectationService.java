package com.schoolSys.schooolSys.affectation;

import java.util.UUID;

import com.schoolSys.schooolSys.affectation.dto.AffectationBulkRequestDTO;
import com.schoolSys.schooolSys.affectation.dto.AffectationBulkResultDTO;
import com.schoolSys.schooolSys.affectation.dto.AffectationDTO;
import com.schoolSys.schooolSys.affectation.dto.AffectationRequestDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.module.ModuleRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AffectationService {

    private final AffectationRepository affectationRepository;
    private final TeacherRepository teacherRepository;
    private final ClasseRepository classeRepository;
    private final ModuleRepository moduleRepository;

    public List<AffectationDTO> search(UUID teacherId, UUID classeId, UUID moduleId, String anneeScolaire) {
        List<Affectation> rows = affectationRepository.search(teacherId, classeId, moduleId, anneeScolaire);
        return hydrate(rows);
    }

    public AffectationDTO findById(UUID id) {
        Affectation a = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", id));
        return hydrate(List.of(a)).get(0);
    }

    @Transactional
    public AffectationDTO create(AffectationRequestDTO req) {
        validate(req, null);
        Affectation a = Affectation.builder()
                .teacherId(req.getTeacherId())
                .classeId(req.getClasseId())
                .moduleId(req.getModuleId())
                .anneeScolaire(req.getAnneeScolaire())
                .dateDebut(req.getDateDebut())
                .dateFin(req.getDateFin())
                .notes(req.getNotes())
                .build();
        Affectation saved = affectationRepository.save(a);
        return hydrate(List.of(saved)).get(0);
    }

    /**
     * Creates many affectations for a single teacher in one transaction. Items that
     * would duplicate an existing assignment are silently skipped (not errors), so
     * re-running a bulk assignment is safe. Referenced classes/modules must exist.
     */
    @Transactional
    public AffectationBulkResultDTO bulkCreate(AffectationBulkRequestDTO req) {
        if (!teacherRepository.existsById(req.getTeacherId())) {
            throw new ResourceNotFoundException("Teacher", req.getTeacherId());
        }
        if (req.getAnneeScolaire() == null || req.getAnneeScolaire().isBlank()) {
            throw new IllegalArgumentException("L'année scolaire est requise.");
        }
        if (req.getDateDebut() != null && req.getDateFin() != null
                && req.getDateFin().isBefore(req.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début.");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Aucune classe sélectionnée.");
        }

        List<Affectation> toSave = new java.util.ArrayList<>();
        java.util.Set<String> seenInBatch = new java.util.HashSet<>();
        int skipped = 0;

        for (AffectationBulkRequestDTO.Item item : req.getItems()) {
            if (item.getClasseId() == null) { skipped++; continue; }
            if (!classeRepository.existsById(item.getClasseId())) { skipped++; continue; }
            if (item.getModuleId() != null && !moduleRepository.existsById(item.getModuleId())) {
                skipped++;
                continue;
            }
            // De-dupe within the submitted batch itself.
            String key = item.getClasseId() + "|" + (item.getModuleId() == null ? "" : item.getModuleId());
            if (!seenInBatch.add(key)) { skipped++; continue; }
            // Skip rows that already exist in the database.
            long dup = affectationRepository.countDuplicate(
                    req.getTeacherId(), item.getClasseId(), item.getModuleId(),
                    req.getAnneeScolaire(), null);
            if (dup > 0) { skipped++; continue; }

            toSave.add(Affectation.builder()
                    .teacherId(req.getTeacherId())
                    .classeId(item.getClasseId())
                    .moduleId(item.getModuleId())
                    .anneeScolaire(req.getAnneeScolaire())
                    .dateDebut(req.getDateDebut())
                    .dateFin(req.getDateFin())
                    .notes(req.getNotes())
                    .build());
        }

        List<Affectation> saved = toSave.isEmpty() ? List.of() : affectationRepository.saveAll(toSave);
        return AffectationBulkResultDTO.builder()
                .created(saved.size())
                .skipped(skipped)
                .affectations(hydrate(saved))
                .build();
    }

    @Transactional
    public AffectationDTO update(UUID id, AffectationRequestDTO req) {
        Affectation existing = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation", id));
        validate(req, id);
        existing.setTeacherId(req.getTeacherId());
        existing.setClasseId(req.getClasseId());
        existing.setModuleId(req.getModuleId());
        existing.setAnneeScolaire(req.getAnneeScolaire());
        existing.setDateDebut(req.getDateDebut());
        existing.setDateFin(req.getDateFin());
        existing.setNotes(req.getNotes());
        Affectation saved = affectationRepository.save(existing);
        return hydrate(List.of(saved)).get(0);
    }

    @Transactional
    public void delete(UUID id) {
        if (!affectationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Affectation", id);
        }
        affectationRepository.deleteById(id);
    }

    /**
     * Validates referenced entities exist, dates are coherent, and the (teacher, classe,
     * module, annee) tuple is unique.
     */
    private void validate(AffectationRequestDTO req, UUID excludeId) {
        if (!teacherRepository.existsById(req.getTeacherId())) {
            throw new ResourceNotFoundException("Teacher", req.getTeacherId());
        }
        if (!classeRepository.existsById(req.getClasseId())) {
            throw new ResourceNotFoundException("Classe", req.getClasseId());
        }
        if (req.getModuleId() != null && !moduleRepository.existsById(req.getModuleId())) {
            throw new ResourceNotFoundException("Module", req.getModuleId());
        }
        if (req.getAnneeScolaire() == null || req.getAnneeScolaire().isBlank()) {
            throw new IllegalArgumentException("L'année scolaire est requise.");
        }
        if (req.getDateDebut() != null && req.getDateFin() != null
                && req.getDateFin().isBefore(req.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début.");
        }
        long dup = affectationRepository.countDuplicate(
                req.getTeacherId(),
                req.getClasseId(),
                req.getModuleId(),
                req.getAnneeScolaire(),
                excludeId
        );
        if (dup > 0) {
            throw new IllegalArgumentException(
                    "Cette affectation existe déjà (même enseignant, classe, matière et année).");
        }
    }

    /**
     * Batch-loads the referenced teachers, classes and modules to populate the DTOs.
     * Cheaper than N+1 lookups in the toDTO helper.
     */
    private List<AffectationDTO> hydrate(List<Affectation> rows) {
        if (rows.isEmpty()) return List.of();

        Map<UUID, String> teacherNames = new HashMap<>();
        teacherRepository.findAllById(rows.stream().map(Affectation::getTeacherId).distinct().toList())
                .forEach(t -> teacherNames.put(t.getId(), buildTeacherName(t)));

        Map<UUID, String> classeNames = new HashMap<>();
        classeRepository.findAllById(rows.stream().map(Affectation::getClasseId).distinct().toList())
                .forEach(c -> classeNames.put(c.getId(), buildClasseName(c)));

        Map<UUID, String> moduleNames = new HashMap<>();
        moduleRepository.findAllById(
                rows.stream().map(Affectation::getModuleId).filter(java.util.Objects::nonNull).distinct().toList()
        ).forEach(m -> moduleNames.put(m.getId(), m.getName()));

        return rows.stream().map(a -> AffectationDTO.builder()
                .id(a.getId())
                .teacherId(a.getTeacherId())
                .teacherName(teacherNames.getOrDefault(a.getTeacherId(), "Inconnu"))
                .classeId(a.getClasseId())
                .classeName(classeNames.getOrDefault(a.getClasseId(), "Inconnue"))
                .moduleId(a.getModuleId())
                .moduleName(a.getModuleId() == null ? null : moduleNames.get(a.getModuleId()))
                .anneeScolaire(a.getAnneeScolaire())
                .dateDebut(a.getDateDebut())
                .dateFin(a.getDateFin())
                .notes(a.getNotes())
                .build()
        ).toList();
    }

    private static String buildTeacherName(Teacher t) {
        String first = t.getFirstName() == null ? "" : t.getFirstName();
        String last = t.getLastName() == null ? "" : t.getLastName();
        return (first + " " + last).trim();
    }

    /** "1A" style: leading digits of niveau name + letter. Falls back to "{niveauName} {letter}". */
    private static String buildClasseName(Classe c) {
        if (c.getNiveau() == null) return c.getLetter();
        String niveauName = c.getNiveau().getName();
        StringBuilder digits = new StringBuilder();
        for (char ch : niveauName.toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        if (digits.length() == 0) return niveauName + " " + c.getLetter();
        return digits.toString() + c.getLetter();
    }
}
