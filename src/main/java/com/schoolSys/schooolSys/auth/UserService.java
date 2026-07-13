package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.dto.UserResponseDTO;
import com.schoolSys.schooolSys.auth.dto.CreateUserRequestDTO;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserContext currentUserContext;
    private final AuditService auditService;

    /**
     * Account-creation hierarchy — which roles each role may create or assign.
     * A creator can never produce an account whose role is not strictly below its own.
     */
    private static final Map<UserRole, Set<UserRole>> CREATABLE_ROLES = Map.of(
        UserRole.SUPER_ADMIN, EnumSet.allOf(UserRole.class),
        UserRole.ADMIN,       EnumSet.of(UserRole.DIRECTEUR, UserRole.ENSEIGNANT,
                                         UserRole.COMPTABLE, UserRole.PARENT),
        UserRole.DIRECTEUR,   EnumSet.of(UserRole.ENSEIGNANT, UserRole.PARENT)
    );

    /** Rejects the call if the current user's role may not manage/assign the given role. */
    private void assertCanAssignRole(UserRole target) {
        UserRole creator = currentUserContext.getRole()
            .orElseThrow(() -> new AccessDeniedException("Authentification requise"));
        if (!CREATABLE_ROLES.getOrDefault(creator, Set.of()).contains(target)) {
            throw new AccessDeniedException(
                "Votre rôle (" + creator + ") ne peut pas gérer un compte de rôle " + target + ".");
        }
    }

    private boolean isSuperAdmin() {
        return currentUserContext.getRole().filter(r -> r == UserRole.SUPER_ADMIN).isPresent();
    }

    /** Non-super-admins always operate within their own school's tenant. */
    private String resolveTenantId(String requestedTenantId) {
        if (isSuperAdmin()) {
            return requestedTenantId;
        }
        return currentUserContext.getUser().map(User::getTenantId).orElse(null);
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        // Super-admin sees everyone; everyone else only their own school (no super-admins).
        if (isSuperAdmin()) {
            return userRepository.findAll(pageable).map(this::toDto);
        }
        String tenantId = currentUserContext.getUser().map(User::getTenantId).orElse(null);
        return userRepository.findByTenantIdAndRoleNot(tenantId, UserRole.SUPER_ADMIN, pageable)
            .map(this::toDto);
    }

    public UserResponseDTO getUserById(UUID id) {
        return toDto(userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        assertCanAssignRole(request.getRole());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .role(request.getRole())
            .tenantId(resolveTenantId(request.getTenantId()))
            .build();
        User saved = userRepository.save(user);
        auditService.log("CREATE", "USER", saved.getId(),
            "Création du compte " + saved.getEmail() + " (rôle " + saved.getRole() + ")");
        return toDto(saved);
    }

    @Transactional
    public UserResponseDTO updateUser(UUID id, CreateUserRequestDTO request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        assertCanAssignRole(user.getRole());      // may the current user manage this account?
        assertCanAssignRole(request.getRole());   // may they assign the requested role?
        UserRole previousRole = user.getRole();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        if (isSuperAdmin()) {
            user.setTenantId(request.getTenantId());   // only super-admin may move a user across tenants
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        User saved = userRepository.save(user);
        String details = "Mise à jour du compte " + saved.getEmail();
        if (previousRole != saved.getRole()) {
            details += " — changement de rôle " + previousRole + " → " + saved.getRole();
        }
        auditService.log("UPDATE", "USER", saved.getId(), details);
        return toDto(saved);
    }

    @Transactional
    public void toggleActive(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        assertCanAssignRole(user.getRole());
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        auditService.log("UPDATE", "USER", user.getId(),
            (user.getIsActive() ? "Activation" : "Désactivation") + " du compte " + user.getEmail());
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        assertCanAssignRole(user.getRole());
        auditService.log("DELETE", "USER", user.getId(),
            "Suppression du compte " + user.getEmail() + " (rôle " + user.getRole() + ")");
        userRepository.delete(user);
    }

    private UserResponseDTO toDto(User u) {
        return UserResponseDTO.builder()
            .id(u.getId()).email(u.getEmail())
            .firstName(u.getFirstName()).lastName(u.getLastName())
            .role(u.getRole()).tenantId(u.getTenantId()).isActive(u.getIsActive())
            .build();
    }
}
