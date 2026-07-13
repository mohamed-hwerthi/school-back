package com.schoolSys.schooolSys.common.security;

import java.util.UUID;

import com.schoolSys.schooolSys.affectation.Affectation;
import com.schoolSys.schooolSys.affectation.AffectationRepository;
import com.schoolSys.schooolSys.auth.User;
import com.schoolSys.schooolSys.auth.UserRepository;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.domaine.Domaine;
import com.schoolSys.schooolSys.module.Module;
import com.schoolSys.schooolSys.module.ModuleRepository;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.parent.ParentStudent;
import com.schoolSys.schooolSys.parent.ParentStudentRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Single source of truth for "who is the current user, and what are they
 * allowed to see" — used by services to apply row-level scoping
 * (ENSEIGNANT sees only his classes, PARENT sees only his children).
 *
 * Permission gating still happens via {@code @PreAuthorize} on controllers;
 * this component only handles row-level filtering inside service methods
 * once the call has been authorized.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserContext {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final AffectationRepository affectationRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final StudentRepository studentRepository;
    private final ClasseRepository classeRepository;
    private final ModuleRepository moduleRepository;

    /** Current authenticated user ID, or empty if anonymous. */
    public Optional<UUID> getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        try {
            return Optional.of(UUID.fromString(auth.getName()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Current authenticated User entity, fetched fresh from DB. */
    public Optional<User> getUser() {
        return getUserId().flatMap(userRepository::findById);
    }

    /** Current user's role, or empty if anonymous. */
    public Optional<UserRole> getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String a = ga.getAuthority();
            if (a.startsWith("ROLE_")) {
                try {
                    return Optional.of(UserRole.valueOf(a.substring(5)));
                } catch (IllegalArgumentException ignored) { /* unknown role */ }
            }
        }
        return Optional.empty();
    }

    public boolean hasRole(UserRole role) {
        return getRole().map(r -> r == role).orElse(false);
    }

    /**
     * Resolve the {@link Teacher} record matching the current user, by email.
     * Returns empty for non-teacher users or if no matching teacher exists yet.
     */
    public Optional<Teacher> getCurrentTeacher() {
        return getUser()
                .filter(u -> u.getRole() == UserRole.ENSEIGNANT)
                .flatMap(u -> teacherRepository.findByEmail(u.getEmail()));
    }

    /** Class IDs the current teacher is affected to (any année scolaire). */
    public Set<UUID> getScopedClasseIdsForTeacher() {
        return getCurrentTeacher()
                .map(t -> affectationRepository.findAll().stream()
                        .filter(a -> t.getId().equals(a.getTeacherId()))
                        .map(Affectation::getClasseId)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    /**
     * Module (matière) IDs the current teacher is explicitly affected to teach.
     * Affectations without a module (professeur principal) contribute nothing —
     * this is the strict "only my subjects" scope.
     */
    public Set<UUID> getScopedModuleIdsForTeacher() {
        return getCurrentTeacher()
                .map(t -> affectationRepository.findAll().stream()
                        .filter(a -> t.getId().equals(a.getTeacherId()))
                        .map(Affectation::getModuleId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    /** Domaine IDs derived from the modules the current teacher teaches. */
    public Set<UUID> getScopedDomaineIdsForTeacher() {
        Set<UUID> moduleIds = getScopedModuleIdsForTeacher();
        if (moduleIds.isEmpty()) return Set.of();
        return moduleRepository.findAllById(moduleIds).stream()
                .map(Module::getDomaine)
                .filter(Objects::nonNull)
                .map(Domaine::getId)
                .collect(Collectors.toSet());
    }

    /** Niveau IDs derived from the classes the current teacher is affected to. */
    public Set<UUID> getScopedNiveauIdsForTeacher() {
        Set<UUID> classeIds = getScopedClasseIdsForTeacher();
        if (classeIds.isEmpty()) return Set.of();
        return classeRepository.findAllById(classeIds).stream()
                .map(c -> c.getNiveau().getId())
                .collect(Collectors.toSet());
    }

    /** True if the current ENSEIGNANT is affected to teach the given moduleId. */
    public boolean teacherTeachesModule(UUID moduleId) {
        if (!hasRole(UserRole.ENSEIGNANT)) return false;
        return getScopedModuleIdsForTeacher().contains(moduleId);
    }

    /**
     * IDs of all students enrolled in the current teacher's classes.
     * Uses the same (niveau name, classe letter) matching rule as
     * {@link #assertCanAccessStudent(UUID)}.
     */
    public Set<UUID> getScopedStudentIdsForTeacher() {
        if (!hasRole(UserRole.ENSEIGNANT)) return Set.of();
        Set<UUID> classeIds = getScopedClasseIdsForTeacher();
        if (classeIds.isEmpty()) return Set.of();
        Set<String> classeKeys = classeRepository.findAllById(classeIds).stream()
                .map(c -> c.getNiveau().getName() + "||" + c.getLetter())
                .collect(Collectors.toSet());
        return studentRepository.findAll().stream()
                .filter(s -> classeKeys.contains(s.getNiveau() + "||" + s.getClasse()))
                .map(Student::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Student IDs the current PARENT is allowed to read.
     * Returns empty for non-parent users.
     */
    public Set<UUID> getScopedStudentIdsForParent() {
        return getUserId()
                .filter(uid -> hasRole(UserRole.PARENT))
                .map(uid -> parentStudentRepository.findByParentUserId(uid).stream()
                        .map(ps -> ps.getStudent().getId())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    /**
     * True if the current PARENT user has access to the given studentId.
     */
    public boolean parentOwnsStudent(UUID studentId) {
        if (!hasRole(UserRole.PARENT)) return false;
        return getScopedStudentIdsForParent().contains(studentId);
    }

    /**
     * True if the current ENSEIGNANT teaches in the given classeId.
     */
    public boolean teacherTeachesClasse(UUID classeId) {
        if (!hasRole(UserRole.ENSEIGNANT)) return false;
        return getScopedClasseIdsForTeacher().contains(classeId);
    }

    /**
     * Convenience: roles that bypass row-level scoping (see everything in tenant).
     */
    public boolean hasUnrestrictedAccess() {
        return getRole()
                .map(r -> r == UserRole.SUPER_ADMIN
                        || r == UserRole.ADMIN
                        || r == UserRole.DIRECTEUR)
                .orElse(false);
    }

    /**
     * Throws AccessDeniedException if the current user is not allowed to read/write
     * data tied to the given student.
     * - SUPER_ADMIN/ADMIN/DIRECTEUR/COMPTABLE: always allowed.
     * - PARENT: only if the student is one of his linked children.
     * - ENSEIGNANT: only if the student is in one of his affected classes.
     */
    public void assertCanAccessStudent(UUID studentId) {
        if (hasUnrestrictedAccess()) return;
        if (hasRole(UserRole.COMPTABLE)) return;
        if (hasRole(UserRole.PARENT)) {
            if (!parentOwnsStudent(studentId)) {
                throw new AccessDeniedException("Cet enfant n'est pas dans votre périmètre.");
            }
            return;
        }
        if (hasRole(UserRole.ENSEIGNANT)) {
            Student s = studentRepository.findById(studentId).orElse(null);
            if (s == null) {
                throw new AccessDeniedException("Élève introuvable ou hors périmètre.");
            }
            Set<UUID> classeIds = getScopedClasseIdsForTeacher();
            List<Classe> myClasses = classeRepository.findAllById(classeIds);
            boolean inScope = myClasses.stream().anyMatch(c ->
                    c.getNiveau().getName().equals(s.getNiveau())
                            && c.getLetter().equals(s.getClasse()));
            if (!inScope) {
                throw new AccessDeniedException("Cet élève n'est pas dans une de vos classes.");
            }
        }
    }

    /** Helper for ParentStudent lookups when needed by callers. */
    public List<ParentStudent> getCurrentParentLinks() {
        return getUserId()
                .filter(uid -> hasRole(UserRole.PARENT))
                .map(parentStudentRepository::findByParentUserId)
                .orElse(List.of());
    }
}
