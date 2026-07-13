package com.schoolSys.schooolSys.niveau;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.niveau.dto.ClasseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClasseController {

    private final ClasseRepository classeRepository;
    private final CurrentUserContext currentUser;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ClasseResponseDTO>>> getAll(
            @RequestParam(required = false) UUID niveauId) {
        List<Classe> classes = niveauId != null
                ? classeRepository.findByNiveauId(niveauId)
                : classeRepository.findAll();

        // Row-level scoping: an ENSEIGNANT only sees classes he is affected to.
        if (currentUser.hasRole(UserRole.ENSEIGNANT)) {
            Set<UUID> scoped = currentUser.getScopedClasseIdsForTeacher();
            classes = classes.stream().filter(c -> scoped.contains(c.getId())).toList();
        }

        List<ClasseResponseDTO> dtos = classes.stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(ClasseResponseDTO::getFullName))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    private ClasseResponseDTO toResponse(Classe c) {
        Niveau n = c.getNiveau();
        StringBuilder digits = new StringBuilder();
        for (char ch : n.getName().toCharArray()) {
            if (Character.isDigit(ch)) digits.append(ch);
            else break;
        }
        String fullName = digits.toString() + c.getLetter();

        return ClasseResponseDTO.builder()
                .id(c.getId())
                .letter(c.getLetter())
                .niveauId(n.getId())
                .niveauName(n.getName())
                .fullName(fullName)
                .build();
    }
}
