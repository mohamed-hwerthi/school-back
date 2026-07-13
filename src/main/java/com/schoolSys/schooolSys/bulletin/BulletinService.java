package com.schoolSys.schooolSys.bulletin;

import java.util.UUID;

import com.schoolSys.schooolSys.appreciation.ObservationRepository;
import com.schoolSys.schooolSys.appreciation.ObservationTrimestre;
import com.schoolSys.schooolSys.appreciation.Recommandation;
import com.schoolSys.schooolSys.appreciation.RecommandationRepository;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.bulletin.dto.*;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.domaine.Domaine;
import com.schoolSys.schooolSys.domaine.DomaineRepository;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.module.ModuleRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.niveau.NiveauRepository;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.settings.SchoolSettings;
import com.schoolSys.schooolSys.settings.SchoolSettingsRepository;
import com.schoolSys.schooolSys.student.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BulletinService {

    private final NoteRepository noteRepository;
    private final ClasseRepository classeRepository;
    private final DomaineRepository domaineRepository;
    private final RecommandationRepository recommandationRepository;
    private final ObservationRepository observationRepository;
    private final BulletinTemplateRepository bulletinTemplateRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final NiveauRepository niveauRepository;
    private final ModuleRepository moduleRepository;
    private final CurrentUserContext currentUser;

    private static final String VERSION_PRIVE = "prive";

    /**
     * Compute bulletins for all students in a class for a given trimestre.
     */
    public List<BulletinDTO> getBulletins(UUID classeId, Integer trimestre, String version) {
        assertTeacherTeachesClasse(classeId);
        boolean prive = VERSION_PRIVE.equalsIgnoreCase(version);

        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe", classeId));
        UUID niveauId = classe.getNiveau().getId();

        // 1. Fetch all notes for this class/trimestre
        List<Note> allNotes = noteRepository.findByExamenClasseIdAndTrimestre(classeId, trimestre);
        if (allNotes.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Fetch domaines for this niveau
        List<Domaine> domaines = domaineRepository.findByNiveauIdOrderByOrdreAsc(niveauId);
        Map<UUID, Domaine> domaineMap = domaines.stream()
                .collect(Collectors.toMap(Domaine::getId, d -> d));

        // 2bis. Fetch ALL modules of the niveau (so the bulletin shows them
        // even when the student has no notes for some of them).
        List<Module> niveauModules = moduleRepository.findByNiveauIdOrderByOrdreEtatiqueAsc(niveauId);

        // 3. Group notes by student
        Map<UUID, List<Note>> notesByStudent = allNotes.stream()
                .collect(Collectors.groupingBy(n -> n.getStudent().getId()));

        List<UUID> studentIds = new ArrayList<>(notesByStudent.keySet());

        // 4. Fetch recommendations & observations
        List<Recommandation> allRecos = recommandationRepository
                .findByStudentIdInAndTrimestre(studentIds, trimestre);
        Map<UUID, List<Recommandation>> recosByStudent = allRecos.stream()
                .collect(Collectors.groupingBy(r -> r.getStudent().getId()));

        List<ObservationTrimestre> allObs = observationRepository
                .findByStudentIdInAndTrimestre(studentIds, trimestre);
        Map<UUID, ObservationTrimestre> obsByStudent = allObs.stream()
                .collect(Collectors.toMap(o -> o.getStudent().getId(), o -> o, (a, b) -> b));

        // 5. Compute per-student module averages (needed for class-level stats)
        // Map<studentId, Map<moduleId, moduleAvg>>
        Map<UUID, Map<UUID, Double>> studentModuleAvgs = new LinkedHashMap<>();
        Map<UUID, Double> studentGeneralAvgs = new LinkedHashMap<>();

        for (Map.Entry<UUID, List<Note>> entry : notesByStudent.entrySet()) {
            UUID studentId = entry.getKey();
            List<Note> studentNotes = entry.getValue();

            Map<UUID, List<Note>> byModule = studentNotes.stream()
                    .collect(Collectors.groupingBy(n -> n.getExamen().getModule().getId()));

            Map<UUID, Double> moduleAvgs = new LinkedHashMap<>();
            // moduleId -> its Module entity (for domaine grouping + coefficients)
            Map<UUID, Module> moduleById = new LinkedHashMap<>();

            for (Map.Entry<UUID, List<Note>> moduleEntry : byModule.entrySet()) {
                List<Note> moduleNotes = moduleEntry.getValue();
                Module module = moduleNotes.get(0).getExamen().getModule();
                moduleById.put(moduleEntry.getKey(), module);

                double sumWeighted = 0;
                double sumCoeff = 0;
                for (Note n : moduleNotes) {
                    if (n.getValeur() != null) {
                        double ec = prive ? n.getExamen().getCoeffPrive() : n.getExamen().getCoeffEtatique();
                        sumWeighted += n.getValeur() * ec;
                        sumCoeff += ec;
                    }
                }

                double avg = sumCoeff > 0 ? round2(sumWeighted / sumCoeff) : 0;
                moduleAvgs.put(moduleEntry.getKey(), avg);
            }

            studentModuleAvgs.put(studentId, moduleAvgs);
            studentGeneralAvgs.put(studentId,
                    computeGeneralAverage(moduleAvgs, moduleById, domaineMap, prive));
        }

        // 6. Compute class-level stats per module
        // Map<moduleId, List<avg>>
        Map<UUID, List<Double>> moduleAvgLists = new LinkedHashMap<>();
        for (Map<UUID, Double> avgs : studentModuleAvgs.values()) {
            for (Map.Entry<UUID, Double> e : avgs.entrySet()) {
                moduleAvgLists.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue());
            }
        }

        Map<UUID, Double> moduleMinMap = new LinkedHashMap<>();
        Map<UUID, Double> moduleMaxMap = new LinkedHashMap<>();
        Map<UUID, Double> moduleClasseAvgMap = new LinkedHashMap<>();

        for (Map.Entry<UUID, List<Double>> e : moduleAvgLists.entrySet()) {
            List<Double> vals = e.getValue();
            moduleMinMap.put(e.getKey(), vals.stream().mapToDouble(Double::doubleValue).min().orElse(0));
            moduleMaxMap.put(e.getKey(), vals.stream().mapToDouble(Double::doubleValue).max().orElse(0));
            moduleClasseAvgMap.put(e.getKey(), round2(vals.stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        }

        // 7. Compute class-level general stats
        List<Double> allGeneralAvgs = new ArrayList<>(studentGeneralAvgs.values());
        double classeAvg = round2(allGeneralAvgs.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        double classeMin = allGeneralAvgs.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double classeMax = allGeneralAvgs.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        int totalEleves = studentGeneralAvgs.size();

        // 8. Compute ranks (descending by moyenne)
        List<Map.Entry<UUID, Double>> ranked = new ArrayList<>(studentGeneralAvgs.entrySet());
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<UUID, Integer> rankMap = new LinkedHashMap<>();
        for (int i = 0; i < ranked.size(); i++) {
            // Same average = same rank
            if (i > 0 && Objects.equals(ranked.get(i).getValue(), ranked.get(i - 1).getValue())) {
                rankMap.put(ranked.get(i).getKey(), rankMap.get(ranked.get(i - 1).getKey()));
            } else {
                rankMap.put(ranked.get(i).getKey(), i + 1);
            }
        }

        // 9. Build the fullName for this classe
        String fullName = buildFullName(classe);

        // 10. Build BulletinDTO per student
        List<BulletinDTO> bulletins = new ArrayList<>();

        for (Map.Entry<UUID, List<Note>> entry : notesByStudent.entrySet()) {
            UUID studentId = entry.getKey();
            List<Note> studentNotes = entry.getValue();
            Student student = studentNotes.get(0).getStudent();

            // Group notes by module
            Map<UUID, List<Note>> byModule = studentNotes.stream()
                    .collect(Collectors.groupingBy(n -> n.getExamen().getModule().getId()));

            // Build module DTOs — start from ALL modules of the niveau so the
            // bulletin always lists them, even when the student has no notes
            // for some modules (cells will display "—" / 0.0 on the front).
            Map<UUID, BulletinModuleDTO> moduleDTOs = new LinkedHashMap<>();
            for (Module module : niveauModules) {
                UUID moduleId = module.getId();
                List<Note> moduleNotes = byModule.getOrDefault(moduleId, Collections.emptyList());

                List<BulletinExamenDTO> examenDTOs = moduleNotes.stream()
                        .sorted(Comparator.comparingInt(n ->
                                prive ? n.getExamen().getOrdrePrive() : n.getExamen().getOrdreEtatique()))
                        .map(n -> BulletinExamenDTO.builder()
                                .examenId(n.getExamen().getId())
                                .examenName(prive && n.getExamen().getNamePrive() != null
                                        ? n.getExamen().getNamePrive() : n.getExamen().getName())
                                .coeff(prive ? n.getExamen().getCoeffPrive() : n.getExamen().getCoeffEtatique())
                                .note(n.getValeur())
                                .build())
                        .toList();

                double moduleAvg = studentModuleAvgs.get(studentId).getOrDefault(moduleId, 0.0);

                moduleDTOs.put(moduleId, BulletinModuleDTO.builder()
                        .moduleId(moduleId)
                        .moduleName(prive && module.getNameVp() != null ? module.getNameVp() : module.getName())
                        .moduleNameAr(module.getNameAr())
                        .sousDomaineId(module.getSousDomaine() != null ? module.getSousDomaine().getId() : null)
                        .sousDomaineName(module.getSousDomaine() != null ? module.getSousDomaine().getName() : null)
                        .sousDomaineNameAr(module.getSousDomaine() != null ? module.getSousDomaine().getNameAr() : null)
                        .sousDomaineOrdre(module.getSousDomaine() != null ? module.getSousDomaine().getOrdre() : null)
                        .coeff(prive ? module.getCoeffPrive() : module.getCoeffEtatique())
                        .ordre(prive ? module.getOrdrePrive() : module.getOrdreEtatique())
                        .examens(examenDTOs)
                        .moyenneModule(moduleAvg)
                        .moduleMin(round2(moduleMinMap.getOrDefault(moduleId, 0.0)))
                        .moduleMax(round2(moduleMaxMap.getOrDefault(moduleId, 0.0)))
                        .moduleClasseAvg(moduleClasseAvgMap.getOrDefault(moduleId, 0.0))
                        .build());
            }

            // Group modules into domaines
            List<BulletinDomaineDTO> domaineDTOs = new ArrayList<>();
            List<BulletinModuleDTO> horsDomaine = new ArrayList<>();

            // Recommendations for this student
            Map<UUID, String> recoTexts = new LinkedHashMap<>();
            List<Recommandation> studentRecos = recosByStudent.getOrDefault(studentId, Collections.emptyList());
            for (Recommandation r : studentRecos) {
                recoTexts.put(r.getDomaine().getId(), r.getTexte());
            }

            // Build a quick lookup moduleId -> Module for domaine filtering
            Map<UUID, Module> niveauModuleMap = niveauModules.stream()
                    .collect(Collectors.toMap(Module::getId, m -> m, (a, b) -> a));

            for (Domaine domaine : domaines) {
                List<BulletinModuleDTO> domaineModules = moduleDTOs.entrySet().stream()
                        .filter(e -> {
                            Module mod = niveauModuleMap.get(e.getKey());
                            return mod != null && mod.getDomaine() != null
                                    && mod.getDomaine().getId().equals(domaine.getId());
                        })
                        .map(Map.Entry::getValue)
                        .sorted(Comparator.comparingInt(BulletinModuleDTO::getOrdre))
                        .toList();

                if (domaineModules.isEmpty()) continue;

                // Domain average = weighted avg of module averages
                double domWeighted = 0, domCoeffSum = 0;
                for (BulletinModuleDTO m : domaineModules) {
                    domWeighted += m.getMoyenneModule() * m.getCoeff();
                    domCoeffSum += m.getCoeff();
                }
                double domaineAvg = domCoeffSum > 0 ? round2(domWeighted / domCoeffSum) : 0;

                domaineDTOs.add(BulletinDomaineDTO.builder()
                        .domaineId(domaine.getId())
                        .domaineName(domaine.getName())
                        .domaineNameAr(domaine.getNameAr())
                        .ordre(domaine.getOrdre())
                        .coeff(prive ? domaine.getCoeffPrive() : domaine.getCoeffEtatique())
                        .modules(domaineModules)
                        .moyenneDomaine(domaineAvg)
                        .recommandation(recoTexts.get(domaine.getId()))
                        .build());
            }

            // Modules not assigned to any domaine
            Set<UUID> assignedModuleIds = domaineDTOs.stream()
                    .flatMap(d -> d.getModules().stream())
                    .map(BulletinModuleDTO::getModuleId)
                    .collect(Collectors.toSet());

            for (Map.Entry<UUID, BulletinModuleDTO> e : moduleDTOs.entrySet()) {
                if (!assignedModuleIds.contains(e.getKey())) {
                    horsDomaine.add(e.getValue());
                }
            }
            horsDomaine.sort(Comparator.comparingInt(BulletinModuleDTO::getOrdre));

            double moyenneGenerale = studentGeneralAvgs.getOrDefault(studentId, 0.0);

            // Observation
            ObservationTrimestre obs = obsByStudent.get(studentId);
            String comportement = obs != null ? obs.getComportement() : null;

            // Certificate is always derived from moyenneGenerale — manual override removed
            // (the teacher only enters moyenne + observation/recommandation, certificat is global)
            String certificat = determineCertificat(moyenneGenerale);

            // Arabic name
            String nameAr = null;
            if (student.getFirstNameAr() != null && student.getLastNameAr() != null) {
                nameAr = student.getFirstNameAr() + " " + student.getLastNameAr();
            }

            bulletins.add(BulletinDTO.builder()
                    .studentId(studentId)
                    .matricule(student.getMatricule())
                    .studentName(student.getFirstName() + " " + student.getLastName())
                    .studentNameAr(nameAr)
                    .classe(fullName)
                    .niveau(classe.getNiveau().getName())
                    .trimestre(trimestre)
                    .version(prive ? "prive" : "etatique")
                    .domaines(domaineDTOs)
                    .modulesHorsDomaine(horsDomaine)
                    .moyenneGenerale(moyenneGenerale)
                    .moyenneClasse(classeAvg)
                    .moyenneMin(round2(classeMin))
                    .moyenneMax(round2(classeMax))
                    .rang(rankMap.getOrDefault(studentId, 0))
                    .totalEleves(totalEleves)
                    .certificatType(certificat)
                    .comportement(comportement)
                    .build());
        }

        bulletins.sort(Comparator.comparing(BulletinDTO::getStudentName));

        // Row-level scoping for non-staff roles.
        if (currentUser.hasRole(UserRole.PARENT)) {
            var ownIds = currentUser.getScopedStudentIdsForParent();
            bulletins = bulletins.stream()
                    .filter(b -> ownIds.contains(b.getStudentId()))
                    .collect(Collectors.toList());
        }
        return bulletins;
    }

    /**
     * Get a single student's bulletin.
     */
    public BulletinDTO getBulletin(UUID classeId, UUID studentId, Integer trimestre, String version) {
        currentUser.assertCanAccessStudent(studentId);
        List<BulletinDTO> all = getBulletins(classeId, trimestre, version);
        return all.stream()
                .filter(b -> b.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin for student", studentId));
    }

    // ── ANN-040: Bulletin annuel (synthèse des 3 trimestres) ──────

    /**
     * Annual bulletin: per-student synthesis of the three trimestre bulletins —
     * per-module and general annual averages, annual rank and mention.
     */
    public List<BulletinAnnuelDTO> getBulletinsAnnuels(UUID classeId, String version) {
        List<BulletinDTO> t1 = getBulletins(classeId, 1, version);
        List<BulletinDTO> t2 = getBulletins(classeId, 2, version);
        List<BulletinDTO> t3 = getBulletins(classeId, 3, version);

        Map<UUID, BulletinDTO> m1 = indexByStudent(t1);
        Map<UUID, BulletinDTO> m2 = indexByStudent(t2);
        Map<UUID, BulletinDTO> m3 = indexByStudent(t3);

        Map<UUID, BulletinDTO> roster = new LinkedHashMap<>();
        for (BulletinDTO b : t1) roster.putIfAbsent(b.getStudentId(), b);
        for (BulletinDTO b : t2) roster.putIfAbsent(b.getStudentId(), b);
        for (BulletinDTO b : t3) roster.putIfAbsent(b.getStudentId(), b);

        List<BulletinAnnuelDTO> result = new ArrayList<>();
        for (Map.Entry<UUID, BulletinDTO> entry : roster.entrySet()) {
            UUID studentId = entry.getKey();
            BulletinDTO ref = entry.getValue();
            BulletinDTO b1 = m1.get(studentId), b2 = m2.get(studentId), b3 = m3.get(studentId);

            Double mg1 = b1 != null ? b1.getMoyenneGenerale() : null;
            Double mg2 = b2 != null ? b2.getMoyenneGenerale() : null;
            Double mg3 = b3 != null ? b3.getMoyenneGenerale() : null;
            Double annuelle = moyenneNonNull(mg1, mg2, mg3);

            Map<UUID, Double[]> moduleVals = new LinkedHashMap<>();
            Map<UUID, String> moduleNames = new LinkedHashMap<>();
            collectModuleAverages(b1, 0, moduleVals, moduleNames);
            collectModuleAverages(b2, 1, moduleVals, moduleNames);
            collectModuleAverages(b3, 2, moduleVals, moduleNames);

            List<ModuleAnnuelDTO> modules = new ArrayList<>();
            for (Map.Entry<UUID, Double[]> me : moduleVals.entrySet()) {
                Double[] v = me.getValue();
                modules.add(ModuleAnnuelDTO.builder()
                        .moduleId(me.getKey())
                        .moduleName(moduleNames.get(me.getKey()))
                        .moyenneT1(v[0]).moyenneT2(v[1]).moyenneT3(v[2])
                        .moyenneAnnuelle(moyenneNonNull(v[0], v[1], v[2]))
                        .build());
            }

            result.add(BulletinAnnuelDTO.builder()
                    .studentId(studentId)
                    .studentName(ref.getStudentName())
                    .studentNameAr(ref.getStudentNameAr())
                    .classe(ref.getClasse())
                    .niveau(ref.getNiveau())
                    .version(ref.getVersion())
                    .moyenneT1(mg1).moyenneT2(mg2).moyenneT3(mg3)
                    .moyenneAnnuelle(annuelle)
                    .mention(mentionAnnuelle(annuelle))
                    .modules(modules)
                    .build());
        }

        // Annual rank — descending by annual average, ties share a rank.
        List<BulletinAnnuelDTO> ranked = result.stream()
                .filter(b -> b.getMoyenneAnnuelle() != null)
                .sorted((a, b) -> Double.compare(b.getMoyenneAnnuelle(), a.getMoyenneAnnuelle()))
                .toList();
        for (int i = 0; i < ranked.size(); i++) {
            if (i > 0 && Objects.equals(ranked.get(i).getMoyenneAnnuelle(),
                    ranked.get(i - 1).getMoyenneAnnuelle())) {
                ranked.get(i).setRang(ranked.get(i - 1).getRang());
            } else {
                ranked.get(i).setRang(i + 1);
            }
        }
        result.forEach(b -> b.setTotalEleves(result.size()));
        result.sort(Comparator.comparing(BulletinAnnuelDTO::getStudentName,
                Comparator.nullsLast(String::compareToIgnoreCase)));
        return result;
    }

    /**
     * ANN-025 — annual success rate per subject: across all students of a class,
     * the share whose annual module average reaches the pass mark (10).
     */
    public List<MatiereStatDTO> getStatsMatieresAnnuelles(UUID classeId, String version) {
        List<BulletinAnnuelDTO> annuels = getBulletinsAnnuels(classeId, version);
        Map<UUID, List<Double>> parModule = new LinkedHashMap<>();
        Map<UUID, String> noms = new LinkedHashMap<>();
        for (BulletinAnnuelDTO b : annuels) {
            if (b.getModules() == null) continue;
            for (ModuleAnnuelDTO m : b.getModules()) {
                if (m.getMoyenneAnnuelle() != null) {
                    parModule.computeIfAbsent(m.getModuleId(), k -> new ArrayList<>())
                            .add(m.getMoyenneAnnuelle());
                    noms.putIfAbsent(m.getModuleId(), m.getModuleName());
                }
            }
        }
        List<MatiereStatDTO> stats = new ArrayList<>();
        for (Map.Entry<UUID, List<Double>> e : parModule.entrySet()) {
            List<Double> vals = e.getValue();
            int reussis = (int) vals.stream().filter(v -> v >= 10).count();
            double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            stats.add(MatiereStatDTO.builder()
                    .moduleId(e.getKey())
                    .moduleName(noms.get(e.getKey()))
                    .moyenne(round2(avg))
                    .reussis(reussis)
                    .echoues(vals.size() - reussis)
                    .taux(vals.isEmpty() ? 0 : round2((double) reussis / vals.size() * 100))
                    .build());
        }
        return stats;
    }

    private Map<UUID, BulletinDTO> indexByStudent(List<BulletinDTO> bulletins) {
        Map<UUID, BulletinDTO> map = new LinkedHashMap<>();
        for (BulletinDTO b : bulletins) map.put(b.getStudentId(), b);
        return map;
    }

    private void collectModuleAverages(BulletinDTO bulletin, int trimIndex,
                                       Map<UUID, Double[]> vals, Map<UUID, String> names) {
        if (bulletin == null) return;
        List<BulletinModuleDTO> modules = new ArrayList<>();
        if (bulletin.getDomaines() != null) {
            for (BulletinDomaineDTO d : bulletin.getDomaines()) {
                if (d.getModules() != null) modules.addAll(d.getModules());
            }
        }
        if (bulletin.getModulesHorsDomaine() != null) {
            modules.addAll(bulletin.getModulesHorsDomaine());
        }
        for (BulletinModuleDTO m : modules) {
            vals.computeIfAbsent(m.getModuleId(), k -> new Double[3])[trimIndex] = m.getMoyenneModule();
            names.putIfAbsent(m.getModuleId(), m.getModuleName());
        }
    }

    /** Mean of the non-null values rounded to 2 decimals; {@code null} when all are null. */
    private Double moyenneNonNull(Double... values) {
        double sum = 0;
        int count = 0;
        for (Double v : values) {
            if (v != null) { sum += v; count++; }
        }
        return count == 0 ? null : round2(sum / count);
    }

    private String mentionAnnuelle(Double moyenne) {
        if (moyenne == null) return null;
        if (moyenne >= 18) return "Excellence";
        if (moyenne >= 16) return "Félicitations";
        if (moyenne >= 14) return "Tableau d'honneur";
        if (moyenne >= 12) return "Encouragements";
        return null;
    }

    // ── BUL-003: Template CRUD ─────────────────────────────

    public List<BulletinTemplateDTO> getAllTemplates() {
        return bulletinTemplateRepository.findAll().stream()
                .map(this::toTemplateDTO)
                .toList();
    }

    public BulletinTemplateDTO getTemplate(UUID id) {
        BulletinTemplate template = bulletinTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BulletinTemplate", id));
        return toTemplateDTO(template);
    }

    public BulletinTemplateDTO getActiveTemplate() {
        return bulletinTemplateRepository.findByActifTrue()
                .map(this::toTemplateDTO)
                .orElse(null);
    }

    @Transactional
    public BulletinTemplateDTO createTemplate(BulletinTemplateDTO dto) {
        BulletinTemplate template = BulletinTemplate.builder()
                .nom(dto.getNom())
                .logoUrl(dto.getLogoUrl())
                .nomEcoleFr(dto.getNomEcoleFr())
                .nomEcoleAr(dto.getNomEcoleAr())
                .adresse(dto.getAdresse())
                .telephone(dto.getTelephone())
                .email(dto.getEmail())
                .headerColor(dto.getHeaderColor() != null ? dto.getHeaderColor() : "#1e3a5f")
                .showLogo(dto.getShowLogo() != null ? dto.getShowLogo() : true)
                .showPhotoEleve(dto.getShowPhotoEleve() != null ? dto.getShowPhotoEleve() : false)
                .showAppreciation(dto.getShowAppreciation() != null ? dto.getShowAppreciation() : true)
                .showRang(dto.getShowRang() != null ? dto.getShowRang() : true)
                .footerText(dto.getFooterText())
                .actif(false)
                .build();
        return toTemplateDTO(bulletinTemplateRepository.save(template));
    }

    @Transactional
    public BulletinTemplateDTO updateTemplate(UUID id, BulletinTemplateDTO dto) {
        BulletinTemplate template = bulletinTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BulletinTemplate", id));
        template.setNom(dto.getNom());
        template.setLogoUrl(dto.getLogoUrl());
        template.setNomEcoleFr(dto.getNomEcoleFr());
        template.setNomEcoleAr(dto.getNomEcoleAr());
        template.setAdresse(dto.getAdresse());
        template.setTelephone(dto.getTelephone());
        template.setEmail(dto.getEmail());
        if (dto.getHeaderColor() != null) template.setHeaderColor(dto.getHeaderColor());
        if (dto.getShowLogo() != null) template.setShowLogo(dto.getShowLogo());
        if (dto.getShowPhotoEleve() != null) template.setShowPhotoEleve(dto.getShowPhotoEleve());
        if (dto.getShowAppreciation() != null) template.setShowAppreciation(dto.getShowAppreciation());
        if (dto.getShowRang() != null) template.setShowRang(dto.getShowRang());
        template.setFooterText(dto.getFooterText());
        template.setUpdatedAt(java.time.LocalDateTime.now());
        return toTemplateDTO(bulletinTemplateRepository.save(template));
    }

    @Transactional
    public BulletinTemplateDTO activateTemplate(UUID id) {
        bulletinTemplateRepository.deactivateAll();
        BulletinTemplate template = bulletinTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BulletinTemplate", id));
        template.setActif(true);
        template.setUpdatedAt(java.time.LocalDateTime.now());
        return toTemplateDTO(bulletinTemplateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        BulletinTemplate template = bulletinTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BulletinTemplate", id));
        if (Boolean.TRUE.equals(template.getActif())) {
            throw new IllegalStateException("Cannot delete the active template");
        }
        bulletinTemplateRepository.delete(template);
    }

    private BulletinTemplateDTO toTemplateDTO(BulletinTemplate t) {
        return BulletinTemplateDTO.builder()
                .id(t.getId())
                .nom(t.getNom())
                .logoUrl(t.getLogoUrl())
                .nomEcoleFr(t.getNomEcoleFr())
                .nomEcoleAr(t.getNomEcoleAr())
                .adresse(t.getAdresse())
                .telephone(t.getTelephone())
                .email(t.getEmail())
                .headerColor(t.getHeaderColor())
                .showLogo(t.getShowLogo())
                .showPhotoEleve(t.getShowPhotoEleve())
                .showAppreciation(t.getShowAppreciation())
                .showRang(t.getShowRang())
                .footerText(t.getFooterText())
                .actif(t.getActif())
                .build();
    }

    // ── BUL-004: Mass generate bulletins ─────────────────

    public List<BulletinDTO> generateBulletinsForClasse(UUID classeId, Integer trimestre) {
        return getBulletins(classeId, trimestre, "etatique");
    }

    // ── BUL-005: Stats reussite ──────────────────────────

    public StatsReussiteDTO getStatsReussite(UUID classeId, Integer trimestre) {
        List<BulletinDTO> bulletins = getBulletins(classeId, trimestre, "etatique");
        if (bulletins.isEmpty()) {
            Classe classe = classeRepository.findById(classeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Classe", classeId));
            return StatsReussiteDTO.builder()
                    .classe(buildFullName(classe))
                    .trimestre(trimestre)
                    .totalEleves(0)
                    .reussis(0)
                    .echoues(0)
                    .tauxReussite(0.0)
                    .moyenneClasse(0.0)
                    .moyenneMin(0.0)
                    .moyenneMax(0.0)
                    .modulesStats(Collections.emptyList())
                    .distribution(Collections.emptyList())
                    .build();
        }

        int total = bulletins.size();
        int reussis = (int) bulletins.stream().filter(b -> b.getMoyenneGenerale() >= 10).count();

        // Module stats
        Map<UUID, List<Double>> moduleAvgs = new LinkedHashMap<>();
        Map<UUID, String> moduleNames = new LinkedHashMap<>();
        for (BulletinDTO b : bulletins) {
            List<BulletinModuleDTO> allModules = new ArrayList<>();
            if (b.getDomaines() != null) {
                for (BulletinDomaineDTO d : b.getDomaines()) {
                    allModules.addAll(d.getModules());
                }
            }
            if (b.getModulesHorsDomaine() != null) {
                allModules.addAll(b.getModulesHorsDomaine());
            }
            for (BulletinModuleDTO m : allModules) {
                moduleAvgs.computeIfAbsent(m.getModuleId(), k -> new ArrayList<>()).add(m.getMoyenneModule());
                moduleNames.putIfAbsent(m.getModuleId(), m.getModuleName());
            }
        }

        List<StatsReussiteDTO.ModuleStatsDTO> modulesStats = new ArrayList<>();
        for (Map.Entry<UUID, List<Double>> entry : moduleAvgs.entrySet()) {
            List<Double> avgs = entry.getValue();
            double avg = round2(avgs.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            double min = avgs.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double max = avgs.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            int modReussis = (int) avgs.stream().filter(v -> v >= 10).count();
            modulesStats.add(StatsReussiteDTO.ModuleStatsDTO.builder()
                    .moduleId(entry.getKey())
                    .moduleName(moduleNames.get(entry.getKey()))
                    .moyenne(avg)
                    .min(round2(min))
                    .max(round2(max))
                    .reussis(modReussis)
                    .echoues(avgs.size() - modReussis)
                    .build());
        }

        // Distribution
        int[] buckets = new int[4]; // 0-5, 5-10, 10-15, 15-20
        for (BulletinDTO b : bulletins) {
            double m = b.getMoyenneGenerale();
            if (m < 5) buckets[0]++;
            else if (m < 10) buckets[1]++;
            else if (m < 15) buckets[2]++;
            else buckets[3]++;
        }
        List<StatsReussiteDTO.DistributionDTO> distribution = List.of(
                StatsReussiteDTO.DistributionDTO.builder().range("0-5").count(buckets[0]).build(),
                StatsReussiteDTO.DistributionDTO.builder().range("5-10").count(buckets[1]).build(),
                StatsReussiteDTO.DistributionDTO.builder().range("10-15").count(buckets[2]).build(),
                StatsReussiteDTO.DistributionDTO.builder().range("15-20").count(buckets[3]).build()
        );

        return StatsReussiteDTO.builder()
                .classe(bulletins.get(0).getClasse())
                .trimestre(trimestre)
                .totalEleves(total)
                .reussis(reussis)
                .echoues(total - reussis)
                .tauxReussite(round2((double) reussis / total * 100))
                .moyenneClasse(bulletins.get(0).getMoyenneClasse())
                .moyenneMin(bulletins.get(0).getMoyenneMin())
                .moyenneMax(bulletins.get(0).getMoyenneMax())
                .modulesStats(modulesStats)
                .distribution(distribution)
                .build();
    }

    // ── BUL-006: Attestation de scolarite ────────────────

    public AttestationDTO getAttestation(UUID eleveId) {
        currentUser.assertCanAccessStudent(eleveId);
        List<Note> notes = noteRepository.findByStudentIdAndTrimestre(eleveId, 1);
        if (notes.isEmpty()) {
            notes = noteRepository.findByStudentIdAndTrimestre(eleveId, 2);
        }
        if (notes.isEmpty()) {
            notes = noteRepository.findByStudentIdAndTrimestre(eleveId, 3);
        }

        Student student;
        String classeName = "";
        String niveauName = "";

        if (!notes.isEmpty()) {
            student = notes.get(0).getStudent();
            Examen examen = notes.get(0).getExamen();
            Classe classe = examen.getClasse();
            classeName = buildFullName(classe);
            niveauName = classe.getNiveau().getName();
        } else {
            throw new ResourceNotFoundException("Student notes not found for attestation", eleveId);
        }

        SchoolSettings settings = schoolSettingsRepository.findAll().stream()
                .findFirst().orElse(SchoolSettings.builder().build());

        String nameAr = null;
        if (student.getFirstNameAr() != null && student.getLastNameAr() != null) {
            nameAr = student.getFirstNameAr() + " " + student.getLastNameAr();
        }

        return AttestationDTO.builder()
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .studentNameAr(nameAr)
                .dateOfBirth(student.getDateOfBirth() != null
                        ? student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null)
                .birthPlace(student.getBirthPlace())
                .registrationNumber(student.getRegistrationNumber())
                .classe(classeName)
                .niveau(niveauName)
                .anneeScolaire(settings.getAnneeScolaire())
                .schoolName(settings.getSchoolName())
                .schoolNameAr(settings.getSchoolNameAr())
                .adresse(settings.getAdresse())
                .telephone(settings.getTelephone())
                .directeurName(settings.getDirecteurName())
                .directeurNameAr(settings.getDirecteurNameAr())
                .dateGeneration(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .build();
    }

    // ── BUL-007: Comparatif performances ─────────────────

    public ComparatifDTO getComparatifByNiveau(UUID niveauId) {
        niveauRepository.findById(niveauId)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", niveauId));

        List<Classe> classes = classeRepository.findByNiveauId(niveauId);
        // A teacher only compares classes he is affected to.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scoped = currentUser.getScopedClasseIdsForTeacher();
            classes = classes.stream()
                    .filter(c -> scoped.contains(c.getId()))
                    .collect(Collectors.toList());
        }
        List<ComparatifDTO.ClassePerformanceDTO> classesPerf = new ArrayList<>();

        for (Classe classe : classes) {
            List<BulletinDTO> bulletins = getBulletins(classe.getId(), 1, "etatique");
            if (bulletins.isEmpty()) continue;

            int total = bulletins.size();
            int classReussis = (int) bulletins.stream().filter(b -> b.getMoyenneGenerale() >= 10).count();

            Map<UUID, List<Double>> modAvgsMap = new LinkedHashMap<>();
            Map<UUID, String> modNamesMap = new LinkedHashMap<>();
            for (BulletinDTO b : bulletins) {
                List<BulletinModuleDTO> allModules = new ArrayList<>();
                if (b.getDomaines() != null) {
                    for (BulletinDomaineDTO d : b.getDomaines()) {
                        allModules.addAll(d.getModules());
                    }
                }
                if (b.getModulesHorsDomaine() != null) {
                    allModules.addAll(b.getModulesHorsDomaine());
                }
                for (BulletinModuleDTO m : allModules) {
                    modAvgsMap.computeIfAbsent(m.getModuleId(), k -> new ArrayList<>()).add(m.getMoyenneModule());
                    modNamesMap.putIfAbsent(m.getModuleId(), m.getModuleName());
                }
            }

            List<ComparatifDTO.ModuleAvgDTO> modAvgs = modAvgsMap.entrySet().stream()
                    .map(e -> ComparatifDTO.ModuleAvgDTO.builder()
                            .moduleId(e.getKey())
                            .moduleName(modNamesMap.get(e.getKey()))
                            .moyenne(round2(e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0)))
                            .build())
                    .toList();

            classesPerf.add(ComparatifDTO.ClassePerformanceDTO.builder()
                    .classeId(classe.getId())
                    .classeName(buildFullName(classe))
                    .moyenneGenerale(bulletins.get(0).getMoyenneClasse())
                    .tauxReussite(round2((double) classReussis / total * 100))
                    .totalEleves(total)
                    .reussis(classReussis)
                    .modulesAvg(modAvgs)
                    .build());
        }

        return ComparatifDTO.builder()
                .classesPerformance(classesPerf)
                .build();
    }

    public ComparatifDTO getComparatifEvolution(UUID classeId) {
        List<ComparatifDTO.EvolutionTrimestreDTO> evolution = new ArrayList<>();

        for (int trimestre = 1; trimestre <= 3; trimestre++) {
            List<BulletinDTO> bulletins = getBulletins(classeId, trimestre, "etatique");
            if (bulletins.isEmpty()) continue;

            int total = bulletins.size();
            int evoReussis = (int) bulletins.stream().filter(b -> b.getMoyenneGenerale() >= 10).count();

            evolution.add(ComparatifDTO.EvolutionTrimestreDTO.builder()
                    .trimestre(trimestre)
                    .moyenneGenerale(bulletins.get(0).getMoyenneClasse())
                    .tauxReussite(round2((double) evoReussis / total * 100))
                    .totalEleves(total)
                    .reussis(evoReussis)
                    .build());
        }

        return ComparatifDTO.builder()
                .evolution(evolution)
                .build();
    }

    // ── Helpers ────────────────────────────────────────────

    /** A teacher may only access bulletins of classes he is affected to. */
    private void assertTeacherTeachesClasse(UUID classeId) {
        if (currentUser.hasRole(UserRole.ENSEIGNANT)
                && !currentUser.teacherTeachesClasse(classeId)) {
            throw new AccessDeniedException("Cette classe n'est pas dans votre périmètre.");
        }
    }

    private String determineCertificat(double moyenne) {
        if (moyenne >= 18) return "شهادة شرف الدرجة الأولى";
        if (moyenne >= 16) return "شهادة شرف";
        if (moyenne >= 14) return "شهادة شكر";
        if (moyenne >= 12) return "شهادة تشجيع";
        return null;
    }

    private String buildFullName(Classe classe) {
        String niveauName = classe.getNiveau().getName();
        StringBuilder digits = new StringBuilder();
        for (char ch : niveauName.toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        return digits.toString() + classe.getLetter();
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    /**
     * General average as the domaine-coefficient-weighted mean of domaine averages.
     * A domaine's own average is the module-coefficient-weighted mean of its graded
     * modules; each domaine then weighs into the general average by its own
     * coefficient (étatique/privé). Modules attached to no domaine fall back to
     * contributing individually, weighted by their module coefficient.
     */
    private double computeGeneralAverage(Map<UUID, Double> moduleAvgs,
                                         Map<UUID, Module> moduleById,
                                         Map<UUID, Domaine> domaineMap,
                                         boolean prive) {
        // domaineId -> [sum(moduleAvg * moduleCoeff), sum(moduleCoeff)]
        Map<UUID, double[]> perDomaine = new LinkedHashMap<>();
        double generalWeighted = 0;
        double generalCoeff = 0;

        for (Map.Entry<UUID, Double> e : moduleAvgs.entrySet()) {
            Module module = moduleById.get(e.getKey());
            double avg = e.getValue();
            double mc = prive ? module.getCoeffPrive() : module.getCoeffEtatique();
            Domaine domaine = module.getDomaine();
            if (domaine != null) {
                double[] acc = perDomaine.computeIfAbsent(domaine.getId(), k -> new double[2]);
                acc[0] += avg * mc;
                acc[1] += mc;
            } else {
                // Orphan module (no domaine): counts as its own unit.
                generalWeighted += avg * mc;
                generalCoeff += mc;
            }
        }

        for (Map.Entry<UUID, double[]> e : perDomaine.entrySet()) {
            double[] acc = e.getValue();
            double domaineAvg = acc[1] > 0 ? acc[0] / acc[1] : 0;
            Domaine domaine = domaineMap.get(e.getKey());
            double dc = domaine == null ? 1.0
                    : (prive ? domaine.getCoeffPrive() : domaine.getCoeffEtatique());
            generalWeighted += domaineAvg * dc;
            generalCoeff += dc;
        }

        return generalCoeff > 0 ? round2(generalWeighted / generalCoeff) : 0.0;
    }
}
