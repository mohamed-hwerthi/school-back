package com.schoolSys.schooolSys.examen;

import java.util.UUID;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import com.schoolSys.schooolSys.anneescolaire.Trimestre;
import com.schoolSys.schooolSys.anneescolaire.TrimestreRepository;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.annee.AnneeScolaireProvider;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.examen.dto.ExamenRequestDTO;
import com.schoolSys.schooolSys.examen.dto.ExamenResponseDTO;
import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.module.ModuleRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final ModuleRepository moduleRepository;
    private final ClasseRepository classeRepository;
    private final TeacherRepository teacherRepository;
    private final NoteRepository noteRepository;
    private final StudentRepository studentRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final TrimestreRepository trimestreRepository;
    private final AnneeScolaireProvider anneeScolaireProvider;
    private final CurrentUserContext currentUser;

    public List<ExamenResponseDTO> findAll(UUID moduleId, UUID classeId, Integer trimestre, String anneeScolaire) {
        String resolved = anneeScolaireProvider.resolveAnneeScolaire(anneeScolaire);
        List<Examen> list = examenRepository.findFiltered(moduleId, classeId, trimestre, resolved);
        // Row-level scoping: an ENSEIGNANT only sees exams in his own classes and subjects.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scopedClasses = currentUser.getScopedClasseIdsForTeacher();
            Set<UUID> scopedModules = currentUser.getScopedModuleIdsForTeacher();
            list = list.stream()
                    .filter(e -> e.getClasse() != null && scopedClasses.contains(e.getClasse().getId()))
                    .filter(e -> e.getModule() != null && scopedModules.contains(e.getModule().getId()))
                    .toList();
        }
        return list.stream()
                .map(this::toResponse)
                .toList();
    }

    public ExamenResponseDTO findById(UUID id) {
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen", id));
        return toResponse(examen);
    }

    @Transactional
    public ExamenResponseDTO create(ExamenRequestDTO dto) {
        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module", dto.getModuleId()));
        Classe classe = classeRepository.findById(dto.getClasseId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe", dto.getClasseId()));

        Teacher teacher = null;
        if (dto.getTeacherId() != null) {
            teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", dto.getTeacherId()));
        }

        Examen examen = Examen.builder()
                .name(dto.getName())
                .namePrive(dto.getNamePrive())
                .coeffEtatique(dto.getCoeffEtatique())
                .coeffPrive(dto.getCoeffPrive())
                .ordreEtatique(dto.getOrdreEtatique())
                .ordrePrive(dto.getOrdrePrive())
                .trimestre(dto.getTrimestre())
                .anneeScolaire(anneeScolaireProvider.getCurrentAnneeScolaire())
                .classe(classe)
                .teacher(teacher)
                .module(module)
                .versionEtatique(dto.getVersionEtatique())
                .versionPrivee(dto.getVersionPrivee())
                .build();

        return toResponse(examenRepository.save(examen));
    }

    /**
     * Builds the default exam grid for a freshly created matière: one exam per
     * (classe of the matière's niveau × trimestre). Idempotent — skips any
     * (classe, trimestre) pair that already has an exam for this module.
     *
     * @return the number of exams created
     */
    @Transactional
    public int createDefaultsForModule(Module module) {
        List<Classe> classes = classeRepository.findByNiveauId(module.getNiveau().getId());
        if (classes.isEmpty()) {
            return 0;
        }
        List<Integer> trimestres = resolveTrimestreNumeros();

        String currentAnnee = anneeScolaireProvider.getCurrentAnneeScolaire();
        List<Examen> toCreate = new ArrayList<>();
        for (Classe classe : classes) {
            for (Integer trimestre : trimestres) {
                boolean alreadyThere = examenRepository
                        .existsByModuleIdAndClasseIdAndTrimestreAndAnneeScolaire(module.getId(), classe.getId(), trimestre, currentAnnee);
                if (alreadyThere) {
                    continue;
                }
                toCreate.add(Examen.builder()
                        .name("Examen")
                        .namePrive("Examen")
                        .coeffEtatique(1.0)
                        .coeffPrive(1.0)
                        .ordreEtatique(1)
                        .ordrePrive(1)
                        .trimestre(trimestre)
                        .anneeScolaire(currentAnnee)
                        .classe(classe)
                        .module(module)
                        .versionEtatique(module.getVersionEtatique())
                        .versionPrivee(module.getVersionPrivee())
                        .build());
            }
        }
        examenRepository.saveAll(toCreate);
        return toCreate.size();
    }

    /**
     * Trimestre numbers to seed exams for: those configured on the active
     * school year, or the standard {1, 2, 3} when none are set up yet.
     */
    private List<Integer> resolveTrimestreNumeros() {
        List<Integer> configured = anneeScolaireRepository.findByActiveTrue()
                .map(annee -> trimestreRepository.findByAnneeScolaireId(annee.getId()).stream()
                        .map(Trimestre::getNumero)
                        .distinct()
                        .sorted()
                        .toList())
                .orElse(List.of());
        return configured.isEmpty() ? List.of(1, 2, 3) : configured;
    }

    @Transactional
    public ExamenResponseDTO update(UUID id, ExamenRequestDTO dto) {
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examen", id));

        Module module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module", dto.getModuleId()));
        Classe classe = classeRepository.findById(dto.getClasseId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe", dto.getClasseId()));

        Teacher teacher = null;
        if (dto.getTeacherId() != null) {
            teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", dto.getTeacherId()));
        }

        examen.setName(dto.getName());
        examen.setNamePrive(dto.getNamePrive());
        examen.setCoeffEtatique(dto.getCoeffEtatique());
        examen.setCoeffPrive(dto.getCoeffPrive());
        examen.setOrdreEtatique(dto.getOrdreEtatique());
        examen.setOrdrePrive(dto.getOrdrePrive());
        examen.setTrimestre(dto.getTrimestre());
        examen.setClasse(classe);
        examen.setTeacher(teacher);
        examen.setModule(module);
        examen.setVersionEtatique(dto.getVersionEtatique());
        examen.setVersionPrivee(dto.getVersionPrivee());

        return toResponse(examenRepository.save(examen));
    }

    @Transactional
    public void delete(UUID id) {
        if (!examenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Examen", id);
        }
        examenRepository.deleteById(id);
    }

    @Transactional
    public void deleteBulk(List<UUID> ids) {
        examenRepository.deleteAllByIdInBatch(ids);
    }

    private ExamenResponseDTO toResponse(Examen examen) {
        String classeName = "";
        // `classeShortName` doit correspondre au format produit par
        // ClasseController.toResponse ("1A") sinon studentRepository.countByClasse
        // ne trouve aucun élève et nbEleves vaut 0 (Aperçu affichait "30/0").
        String classeShortName = "";
        if (examen.getClasse() != null && examen.getClasse().getNiveau() != null) {
            String niveauName = examen.getClasse().getNiveau().getName();
            String prefix = niveauName.replaceAll("[^0-9]", "");
            classeName = prefix + "ème " + examen.getClasse().getLetter();
            StringBuilder digits = new StringBuilder();
            for (char ch : niveauName.toCharArray()) {
                if (Character.isDigit(ch)) digits.append(ch);
                else break;
            }
            classeShortName = digits.toString() + examen.getClasse().getLetter();
        }

        String teacherName = "";
        if (examen.getTeacher() != null) {
            teacherName = examen.getTeacher().getFirstName() + " " + examen.getTeacher().getLastName();
        }

        long nbNotes = noteRepository.countByExamenIdAndTrimestre(examen.getId(), examen.getTrimestre());
        long nbEleves = classeShortName.isEmpty() ? 0 : studentRepository.countByClasse(classeShortName);

        return ExamenResponseDTO.builder()
                .id(examen.getId())
                .name(examen.getName())
                .namePrive(examen.getNamePrive())
                .coeffEtatique(examen.getCoeffEtatique())
                .coeffPrive(examen.getCoeffPrive())
                .ordreEtatique(examen.getOrdreEtatique())
                .ordrePrive(examen.getOrdrePrive())
                .trimestre(examen.getTrimestre())
                .classeId(examen.getClasse().getId())
                .classeName(classeName)
                .teacherId(examen.getTeacher() != null ? examen.getTeacher().getId() : null)
                .teacherName(teacherName)
                .moduleId(examen.getModule().getId())
                .moduleName(examen.getModule().getName())
                .versionEtatique(examen.getVersionEtatique())
                .versionPrivee(examen.getVersionPrivee())
                .anneeScolaire(examen.getAnneeScolaire())
                .nbNotes(nbNotes)
                .nbEleves(nbEleves)
                .build();
    }
}
