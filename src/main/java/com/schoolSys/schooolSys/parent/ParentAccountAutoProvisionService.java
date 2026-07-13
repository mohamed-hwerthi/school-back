package com.schoolSys.schooolSys.parent;

import com.schoolSys.schooolSys.auth.User;
import com.schoolSys.schooolSys.auth.UserRepository;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.student.Student;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Idempotently ensures a PARENT user account exists for a student's parent
 * email, and that the parent_students link row is in place.
 *
 * <p>Email collision with a non-PARENT account is left untouched (we don't
 * mutate an existing user's role) — admin will need to resolve manually.</p>
 */
@Service
@RequiredArgsConstructor
public class ParentAccountAutoProvisionService {

    private static final Logger log = LoggerFactory.getLogger(ParentAccountAutoProvisionService.class);

    private final UserRepository userRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Runs in its own transaction so a parent-provisioning failure never
     * aborts the surrounding student-save transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureLinked(Student student) {
        if (student == null || student.getId() == null) return;
        String email = student.getParentEmail();
        if (email == null || email.isBlank()) return;

        try {
            User parent = userRepository.findByEmail(email)
                    .orElseGet(() -> createParentUser(student));

            if (parent.getRole() != UserRole.PARENT) {
                log.warn("Skipping parent link for student {}: email {} already used by role {}",
                        student.getId(), email, parent.getRole());
                return;
            }

            boolean alreadyLinked = parentStudentRepository.findByStudentId(student.getId()).stream()
                    .anyMatch(ps -> parent.getId().equals(ps.getParentUserId()));
            if (alreadyLinked) return;

            parentStudentRepository.save(ParentStudent.builder()
                    .parentUserId(parent.getId())
                    .student(student)
                    .build());
        } catch (Exception ex) {
            // Don't propagate — parent provisioning is best-effort.
            log.error("Parent auto-provisioning failed for student {} ({}): {}",
                    student.getId(), email, ex.getMessage());
        }
    }

    private User createParentUser(Student student) {
        // Random 12-char temp password — parent will reset via the password-reset flow.
        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        User parent = User.builder()
                .email(student.getParentEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .firstName(orElse(student.getParentFirstName(), "Parent"))
                .lastName(orElse(student.getParentLastName(), student.getLastName()))
                .role(UserRole.PARENT)
                .tenantId(TenantContext.getCurrentTenant())
                .build();
        User saved = userRepository.save(parent);
        auditService.log("CREATE", "USER", saved.getId(),
                "Auto-création du compte PARENT " + saved.getEmail()
                        + " lors de l'inscription de " + student.getFirstName() + " " + student.getLastName());
        return saved;
    }

    private static String orElse(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
