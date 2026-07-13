package com.schoolSys.schooolSys.student;

import java.util.UUID;

import com.schoolSys.schooolSys.parent.ParentAccountAutoProvisionService;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * Per-row save helper. Each call runs in its own brand-new transaction
 * (REQUIRES_NEW) so a row that hits a unique constraint rolls back ONLY
 * itself, leaving the outer batch loop free to keep going.
 *
 * <p>This must be a separate Spring bean because @Transactional only kicks
 * in when invoked through a proxy — calling a private method on the same
 * service class would bypass the AOP wrapper.</p>
 */
@Component
@RequiredArgsConstructor
public class StudentImportRowSaver {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final ParentAccountAutoProvisionService parentAutoProvisioner;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Student saveNew(StudentRequestDTO dto) {
        Student student = studentMapper.toEntity(dto);
        student.setMatricule(generateMatricule());
        Student saved = studentRepository.save(student);
        parentAutoProvisioner.ensureLinked(saved);
        return saved;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Student updateExisting(UUID id, StudentRequestDTO dto) {
        Student existing = studentRepository.findById(id).orElseThrow();
        String matricule = existing.getMatricule();
        studentMapper.updateEntity(dto, existing);
        if (matricule != null) existing.setMatricule(matricule);
        Student saved = studentRepository.save(existing);
        parentAutoProvisioner.ensureLinked(saved);
        return saved;
    }

    private String generateMatricule() {
        Long seq = ((Number) entityManager
                .createNativeQuery("SELECT nextval('matricule_seq')")
                .getSingleResult()).longValue();
        return String.format("ELV-%d-%05d", Year.now().getValue(), seq);
    }
}
