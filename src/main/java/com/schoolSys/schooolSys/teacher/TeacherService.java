package com.schoolSys.schooolSys.teacher;

import com.schoolSys.schooolSys.auth.PasswordResetService;
import com.schoolSys.schooolSys.auth.User;
import com.schoolSys.schooolSys.auth.UserRepository;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.teacher.dto.TeacherRequestDTO;
import com.schoolSys.schooolSys.teacher.dto.TeacherResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherMapper teacherMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    public List<TeacherResponseDTO> findAll() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toResponseDTO)
                .toList();
    }

    public PagedResponse<TeacherResponseDTO> findPage(String search, String statut, int page, int size) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        String normalizedStatut = (statut == null || statut.isBlank()) ? null : statut.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "lastName", "firstName"));
        Page<Teacher> result = teacherRepository.findFiltered(normalizedSearch, normalizedStatut, pageable);
        List<TeacherResponseDTO> dtos = result.getContent().stream()
                .map(teacherMapper::toResponseDTO)
                .toList();
        return PagedResponse.from(result, dtos);
    }

    public TeacherResponseDTO findById(UUID id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", id));
        return teacherMapper.toResponseDTO(teacher);
    }

    @Transactional
    public TeacherResponseDTO create(TeacherRequestDTO dto) {
        String email = dto.getEmail();
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "Un compte utilisateur existe déjà avec l'email « " + email + " ». "
                + "Utilisez « Mot de passe oublié » sur la page de connexion pour récupérer l'accès."
            );
        }
        Teacher teacher = teacherMapper.toEntity(dto);
        Teacher saved = teacherRepository.save(teacher);
        provisionTeacherAccount(saved);
        return teacherMapper.toResponseDTO(saved);
    }

    @Transactional
    public TeacherResponseDTO update(UUID id, TeacherRequestDTO dto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", id));
        teacherMapper.updateEntity(dto, teacher);
        return teacherMapper.toResponseDTO(teacherRepository.save(teacher));
    }

    @Transactional
    public void delete(UUID id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher", id);
        }
        teacherRepository.deleteById(id);
    }

    @Transactional
    public List<TeacherResponseDTO> importBulk(List<TeacherRequestDTO> dtos) {
        List<Teacher> teachers = dtos.stream()
                .map(teacherMapper::toEntity)
                .toList();
        List<Teacher> saved = teacherRepository.saveAll(teachers);
        saved.forEach(this::provisionTeacherAccount);
        return saved.stream()
                .map(teacherMapper::toResponseDTO)
                .toList();
    }

    /**
     * Creates the linked ENSEIGNANT user account for a teacher and sends an
     * activation email so they can set their own password (SEC-029).
     * <p>
     * No-op when the teacher has no email or already has an account, so it is
     * safe to call on (re-)import. An email failure never blocks creation.
     * </p>
     */
    private void provisionTeacherAccount(Teacher teacher) {
        String email = teacher.getEmail();
        if (email == null || email.isBlank() || userRepository.existsByEmail(email)) {
            return;
        }
        User account = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .role(UserRole.ENSEIGNANT)
                .tenantId(TenantContext.getCurrentTenant())
                .build();
        userRepository.save(account);
        try {
            passwordResetService.requestPasswordReset(email);
        } catch (Exception emailFailure) {
            log.warn("Email d'activation non envoyé à {}: {}", email, emailFailure.getMessage());
        }
    }
}
