package com.schoolSys.schooolSys.niveau;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.emploidutemps.EmploiDuTempsRepository;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.niveau.dto.NiveauResponseDTO;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NiveauService {

    private final NiveauRepository niveauRepository;
    private final StudentRepository studentRepository;
    private final ExamenRepository examenRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final CurrentUserContext currentUser;

    public List<NiveauResponseDTO> findAll() {
        List<Niveau> list = niveauRepository.findAll();
        // Row-level scoping: an ENSEIGNANT only sees the niveaux of his own classes.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scoped = currentUser.getScopedNiveauIdsForTeacher();
            list = list.stream().filter(n -> scoped.contains(n.getId())).toList();
        }
        return list.stream()
                .map(this::toResponse)
                .toList();
    }

    public NiveauResponseDTO findById(UUID id) {
        Niveau niveau = niveauRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", id));
        return toResponse(niveau);
    }

    @Transactional
    public NiveauResponseDTO create(String name) {
        if (niveauRepository.existsByName(name)) {
            throw new IllegalArgumentException("Le niveau \"" + name + "\" existe déjà");
        }
        Niveau niveau = Niveau.builder().name(name).build();
        // Add default section "A"
        Classe defaultClasse = Classe.builder().niveau(niveau).letter("A").build();
        niveau.getClasses().add(defaultClasse);
        return toResponse(niveauRepository.save(niveau));
    }

    @Transactional
    public void delete(UUID id) {
        if (!niveauRepository.existsById(id)) {
            throw new ResourceNotFoundException("Niveau", id);
        }
        niveauRepository.deleteById(id);
    }

    @Transactional
    public NiveauResponseDTO addClasse(UUID niveauId, String letter) {
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", niveauId));

        String upper = letter.toUpperCase().trim();
        boolean exists = niveau.getClasses().stream()
                .anyMatch(c -> c.getLetter().equals(upper));
        if (exists) {
            throw new IllegalArgumentException("La section \"" + upper + "\" existe déjà dans ce niveau");
        }

        Classe classe = Classe.builder().niveau(niveau).letter(upper).build();
        niveau.getClasses().add(classe);
        return toResponse(niveauRepository.save(niveau));
    }

    @Transactional
    public NiveauResponseDTO removeClasse(UUID niveauId, String letter) {
        Niveau niveau = niveauRepository.findById(niveauId)
                .orElseThrow(() -> new ResourceNotFoundException("Niveau", niveauId));

        String upper = letter.toUpperCase().trim();
        Classe target = niveau.getClasses().stream()
                .filter(c -> c.getLetter().equals(upper))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "La section \"" + upper + "\" n'existe pas dans ce niveau"));

        String fullName = buildFullName(niveau.getName(), upper);
        long studentCount = studentRepository.countByClasse(fullName);
        long examenCount = examenRepository.findFiltered(null, target.getId(), null, null).size();
        long emploiCount = emploiDuTempsRepository.findByClasseId(target.getId()).size();

        if (studentCount + examenCount + emploiCount > 0) {
            List<String> reasons = new ArrayList<>();
            if (studentCount > 0) reasons.add(studentCount + " élève(s) inscrit(s)");
            if (examenCount > 0) reasons.add(examenCount + " examen(s)");
            if (emploiCount > 0) reasons.add(emploiCount + " entrée(s) d'emploi du temps");
            throw new IllegalStateException(
                    "Impossible de supprimer la section \"" + fullName + "\" : "
                            + String.join(", ", reasons)
                            + ". Réaffectez ces données avant de supprimer la section.");
        }

        niveau.getClasses().remove(target);
        return toResponse(niveauRepository.save(niveau));
    }

    private String buildFullName(String niveauName, String letter) {
        StringBuilder digits = new StringBuilder();
        for (char ch : niveauName.toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        return digits.toString() + letter;
    }

    private NiveauResponseDTO toResponse(Niveau niveau) {
        List<String> sections = niveau.getClasses().stream()
                .map(Classe::getLetter)
                .sorted()
                .toList();
        return NiveauResponseDTO.builder()
                .id(niveau.getId())
                .name(niveau.getName())
                .sections(sections)
                .build();
    }
}
