package com.schoolSys.schooolSys.student;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import com.schoolSys.schooolSys.niveau.Classe;
import com.schoolSys.schooolSys.niveau.ClasseRepository;
import com.schoolSys.schooolSys.student.dto.ReinscriptionRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentBulkImportResultDTO;
import com.schoolSys.schooolSys.student.dto.StudentBulkImportResultDTO.RowError;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentResponseDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final StudentImportRowSaver rowSaver;
    private final CurrentUserContext currentUser;
    private final ClasseRepository classeRepository;
    private final com.schoolSys.schooolSys.parent.ParentAccountAutoProvisionService parentAutoProvisioner;

    @PersistenceContext
    private EntityManager entityManager;

    public List<StudentResponseDTO> findAll() {
        Specification<Student> scope = currentUserScope();
        return studentRepository.findAll(scope).stream()
                .map(studentMapper::toResponseDTO)
                .toList();
    }

    public PagedResponse<StudentResponseDTO> findAll(
            String search,
            String niveau,
            String classe,
            String status,
            String sex,
            Boolean blocked,
            Pageable pageable
    ) {
        Specification<Student> spec = Specification
                .where(StudentSpecification.search(search))
                .and(StudentSpecification.hasNiveau(niveau))
                .and(StudentSpecification.hasClasse(classe))
                .and(StudentSpecification.hasStatus(status))
                .and(StudentSpecification.hasSex(sex))
                .and(StudentSpecification.isBlocked(blocked))
                .and(currentUserScope());

        Page<Student> page = studentRepository.findAll(spec, pageable);
        List<StudentResponseDTO> content = page.getContent().stream()
                .map(studentMapper::toResponseDTO)
                .toList();

        return PagedResponse.from(page, content);
    }

    public StudentResponseDTO findById(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        assertCurrentUserCanRead(student);
        return studentMapper.toResponseDTO(student);
    }

    /**
     * Builds a Specification that restricts results to what the current user is
     * allowed to see. SUPER_ADMIN/ADMIN/DIRECTEUR get an open scope.
     * ENSEIGNANT is restricted to students of his affected classes.
     * PARENT is restricted to his linked children.
     */
    private Specification<Student> currentUserScope() {
        if (currentUser.hasUnrestrictedAccess()) {
            return (r, q, cb) -> cb.conjunction();
        }
        if (currentUser.hasRole(com.schoolSys.schooolSys.auth.UserRole.ENSEIGNANT)) {
            Set<UUID> classeIds = currentUser.getScopedClasseIdsForTeacher();
            List<StudentSpecification.NiveauClasse> tuples = classeRepository.findAllById(classeIds)
                    .stream()
                    .map(c -> new StudentSpecification.NiveauClasse(
                            c.getNiveau().getName(), c.getLetter()))
                    .toList();
            return StudentSpecification.inAnyOf(tuples);
        }
        if (currentUser.hasRole(com.schoolSys.schooolSys.auth.UserRole.PARENT)) {
            return StudentSpecification.hasIdIn(
                    currentUser.getScopedStudentIdsForParent());
        }
        // COMPTABLE and other roles: read-only access (full scope, controller already restricts to READ_STUDENTS).
        return (r, q, cb) -> cb.conjunction();
    }

    private void assertCurrentUserCanRead(Student student) {
        if (currentUser.hasUnrestrictedAccess()) return;
        if (currentUser.hasRole(com.schoolSys.schooolSys.auth.UserRole.PARENT)
                && !currentUser.parentOwnsStudent(student.getId())) {
            throw new AccessDeniedException("Cet enfant n'est pas dans votre périmètre.");
        }
        if (currentUser.hasRole(com.schoolSys.schooolSys.auth.UserRole.ENSEIGNANT)) {
            Set<UUID> classeIds = currentUser.getScopedClasseIdsForTeacher();
            List<Classe> myClasses = classeRepository.findAllById(classeIds);
            boolean inScope = myClasses.stream().anyMatch(c ->
                    c.getNiveau().getName().equals(student.getNiveau())
                            && c.getLetter().equals(student.getClasse()));
            if (!inScope) {
                throw new AccessDeniedException("Cet élève n'est pas dans une de vos classes.");
            }
        }
    }

    @Transactional
    public StudentResponseDTO create(StudentRequestDTO dto) {
        Student student = studentMapper.toEntity(dto);
        student.setMatricule(generateMatricule());
        Student saved = studentRepository.save(student);
        parentAutoProvisioner.ensureLinked(saved);
        return studentMapper.toResponseDTO(saved);
    }

    private String generateMatricule() {
        Long seq = ((Number) entityManager
                .createNativeQuery("SELECT nextval('matricule_seq')")
                .getSingleResult()).longValue();
        return String.format("ELV-%d-%05d", Year.now().getValue(), seq);
    }

    @Transactional
    public StudentResponseDTO update(UUID id, StudentRequestDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        String existingMatricule = student.getMatricule();
        studentMapper.updateEntity(dto, student);
        // Preserve the auto-generated matricule
        if (existingMatricule != null) {
            student.setMatricule(existingMatricule);
        }
        Student saved = studentRepository.save(student);
        parentAutoProvisioner.ensureLinked(saved);
        return studentMapper.toResponseDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", id);
        }
        studentRepository.deleteById(id);
    }

    @Transactional
    public List<StudentResponseDTO> importBulk(List<StudentRequestDTO> dtos) {
        List<Student> students = dtos.stream()
                .map(dto -> {
                    Student s = studentMapper.toEntity(dto);
                    s.setMatricule(generateMatricule());
                    return s;
                })
                .toList();
        return studentRepository.saveAll(students).stream()
                .map(studentMapper::toResponseDTO)
                .toList();
    }

    /**
     * Robust bulk import: each row is persisted in its own implicit transaction
     * (via Spring Data's per-method @Transactional on save) so a single bad row
     * never aborts the whole batch. Returns a structured per-row report.
     *
     * Default strategy is SKIP via the no-arg overload.
     */
    public StudentBulkImportResultDTO importBulkRobust(List<StudentRequestDTO> dtos) {
        return importBulkRobust(dtos, "SKIP");
    }

    /**
     * @param strategy "SKIP" (default) — keep the existing record on email conflict.
     *                 "UPDATE" — overwrite the existing record with the import data.
     */
    public StudentBulkImportResultDTO importBulkRobust(List<StudentRequestDTO> dtos, String strategy) {
        boolean updateOnConflict = "UPDATE".equalsIgnoreCase(strategy);
        int created = 0;
        int skipped = 0;
        int failed = 0;
        List<RowError> errors = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i++) {
            int rowNum = i + 1;
            StudentRequestDTO dto = dtos.get(i);

            try {
                UUID existingId = null;
                if (dto.getEmail() != null && !dto.getEmail().isBlank()
                        && studentRepository.existsByEmail(dto.getEmail())) {
                    if (!updateOnConflict) {
                        skipped++;
                        errors.add(RowError.builder()
                                .row(rowNum)
                                .field("email")
                                .message("Email déjà utilisé — ligne ignorée (stratégie SKIP)")
                                .code("DUPLICATE")
                                .build());
                        continue;
                    }
                    // UPDATE strategy: locate the existing record by email
                    existingId = studentRepository.findAll().stream()
                            .filter(s -> dto.getEmail().equalsIgnoreCase(s.getEmail()))
                            .map(Student::getId)
                            .findFirst()
                            .orElse(null);
                }

                if (existingId != null) {
                    rowSaver.updateExisting(existingId, dto);
                    created++; // upsert success
                    continue;
                }

                rowSaver.saveNew(dto);
                created++;

            } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                // Most likely a unique-constraint hit not caught by the pre-check above
                failed++;
                String msg = ex.getMostSpecificCause() != null
                        ? ex.getMostSpecificCause().getMessage()
                        : ex.getMessage();
                errors.add(RowError.builder()
                        .row(rowNum)
                        .message("Violation de contrainte : " + msg)
                        .code("DUPLICATE")
                        .build());
            } catch (Exception ex) {
                failed++;
                errors.add(RowError.builder()
                        .row(rowNum)
                        .message(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName())
                        .code("ERROR")
                        .build());
            }
        }

        return StudentBulkImportResultDTO.builder()
                .created(created)
                .skipped(skipped)
                .failed(failed)
                .errors(errors)
                .build();
    }

    // ===================== ELV-009: Reinscription en masse =====================

    @Transactional
    public List<StudentResponseDTO> reinscriptionMasse(ReinscriptionRequestDTO dto) {
        List<Student> students = studentRepository.findAllById(dto.getStudentIds());
        for (Student student : students) {
            student.setClasse(dto.getNewClasse());
            if (dto.getNewNiveau() != null && !dto.getNewNiveau().isBlank()) {
                student.setNiveau(dto.getNewNiveau());
            }
            student.setEnrollmentDate(LocalDate.now());
            student.setStatus("Actif");
        }
        return studentRepository.saveAll(students).stream()
                .map(studentMapper::toResponseDTO)
                .toList();
    }

    // ===================== ELV-010: Excel import template =====================

    public byte[] generateImportTemplate() {
        // Generate a simple CSV template (tab-separated) that can be opened in Excel
        // Using CSV avoids needing Apache POI dependency
        StringBuilder sb = new StringBuilder();
        sb.append("firstName\tlastName\tfirstNameAr\tlastNameAr\tsex\tdateOfBirth\tbirthPlace\taddress\temail\tclasse\tniveau\tstatus\tparentLastName\tparentFirstName\tparentPhone\tparentEmail\tnotes\n");
        sb.append("Ahmed\tBenali\tأحمد\tبن علي\tM\t2015-09-15\tTunis\t10 Rue de la Liberté\t\t1A\t1ère année\tActif\tBenali\tMohamed\t06123456\tparent@email.com\t\n");
        sb.append("Fatma\tBouazizi\tفاطمة\tبوعزيزي\tF\t2016-03-22\tSousse\t25 Avenue Bourguiba\t\t1B\t1ère année\tActif\tBouazizi\tAli\t07654321\t\t\n");
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ===================== ELV-012: Fiche eleve PDF (HTML) =====================

    public String generateFicheHtml(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        String sexeLabel = "M".equals(student.getSex()) ? "Masculin" : "Féminin";
        String dateNaissance = student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : "—";
        String dateInscription = student.getEnrollmentDate() != null ? student.getEnrollmentDate().toString() : "—";

        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                <meta charset="UTF-8" />
                <title>Fiche Eleve - %s %s</title>
                <style>
                  @page { size: A4; margin: 15mm; }
                  * { margin: 0; padding: 0; box-sizing: border-box; }
                  body {
                    font-family: 'Segoe UI', Arial, sans-serif;
                    font-size: 11pt;
                    color: #222;
                    background: #fff;
                    -webkit-print-color-adjust: exact;
                    print-color-adjust: exact;
                  }
                  .page {
                    max-width: 190mm;
                    margin: 10mm auto;
                    border: 1px solid #ccc;
                    padding: 12mm;
                  }
                  .header {
                    text-align: center;
                    margin-bottom: 8mm;
                    border-bottom: 2px solid #2563eb;
                    padding-bottom: 5mm;
                  }
                  .header h1 {
                    font-size: 18pt;
                    color: #2563eb;
                    margin-bottom: 2mm;
                  }
                  .header .matricule {
                    font-size: 12pt;
                    color: #666;
                    font-family: monospace;
                  }
                  .section {
                    margin-bottom: 6mm;
                  }
                  .section h2 {
                    font-size: 13pt;
                    color: #2563eb;
                    border-bottom: 1px solid #e0e0e0;
                    padding-bottom: 2mm;
                    margin-bottom: 3mm;
                  }
                  .grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 2mm 10mm;
                  }
                  .field {
                    padding: 1.5mm 0;
                  }
                  .field .label {
                    font-size: 9pt;
                    color: #888;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                  }
                  .field .value {
                    font-weight: 600;
                    font-size: 11pt;
                  }
                  .status-badge {
                    display: inline-block;
                    padding: 1mm 4mm;
                    border-radius: 3mm;
                    font-size: 10pt;
                    font-weight: 600;
                  }
                  .status-actif { background: #d1fae5; color: #065f46; }
                  .status-inactif { background: #fee2e2; color: #991b1b; }
                  .status-attente { background: #fef3c7; color: #92400e; }
                  .footer {
                    margin-top: 10mm;
                    text-align: center;
                    font-size: 9pt;
                    color: #aaa;
                    border-top: 1px solid #e0e0e0;
                    padding-top: 3mm;
                  }
                  .no-print { text-align: center; margin: 5mm 0; }
                  @media print {
                    .no-print { display: none !important; }
                    .page { border: none; margin: 0; }
                  }
                </style>
                </head>
                <body>
                <div class="page">
                  <div class="header">
                    <h1>Fiche de l'Eleve</h1>
                    <div class="matricule">%s</div>
                  </div>

                  <div class="section">
                    <h2>Informations personnelles</h2>
                    <div class="grid">
                      <div class="field"><div class="label">Nom</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Prenom</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Nom (Arabe)</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Prenom (Arabe)</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Sexe</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Date de naissance</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Lieu de naissance</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Adresse</div><div class="value">%s</div></div>
                    </div>
                  </div>

                  <div class="section">
                    <h2>Informations scolaires</h2>
                    <div class="grid">
                      <div class="field"><div class="label">Matricule</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Classe</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Niveau</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Date d'inscription</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Statut</div><div class="value"><span class="status-badge %s">%s</span></div></div>
                    </div>
                  </div>

                  <div class="section">
                    <h2>Parent / Tuteur</h2>
                    <div class="grid">
                      <div class="field"><div class="label">Nom du parent</div><div class="value">%s %s</div></div>
                      <div class="field"><div class="label">Telephone</div><div class="value">%s</div></div>
                      <div class="field"><div class="label">Email</div><div class="value">%s</div></div>
                    </div>
                  </div>

                  %s

                  <div class="footer">
                    Document genere le %s
                  </div>
                </div>

                <div class="no-print">
                  <button onclick="window.print()" style="padding:10px 30px;font-size:14px;cursor:pointer;background:#2563eb;color:white;border:none;border-radius:6px;">
                    Imprimer / Telecharger PDF
                  </button>
                </div>
                </body>
                </html>
                """.formatted(
                safe(student.getFirstName()), safe(student.getLastName()),
                safe(student.getMatricule()),
                safe(student.getLastName()), safe(student.getFirstName()),
                safe(student.getLastNameAr()), safe(student.getFirstNameAr()),
                sexeLabel, dateNaissance,
                safe(student.getBirthPlace()), safe(student.getAddress()),
                safe(student.getMatricule()), safe(student.getClasse()),
                safe(student.getNiveau()), dateInscription,
                statusCssClass(student.getStatus()), safe(student.getStatus()),
                safe(student.getParentFirstName()), safe(student.getParentLastName()),
                safe(student.getParentPhone()), safe(student.getParentEmail()),
                student.getNotes() != null && !student.getNotes().isBlank()
                        ? "<div class=\"section\"><h2>Notes / Observations</h2><p>" + safe(student.getNotes()) + "</p></div>"
                        : "",
                LocalDate.now().toString()
        );
    }

    private String safe(String val) {
        return val != null ? val : "—";
    }

    private String statusCssClass(String status) {
        if (status == null) return "";
        return switch (status) {
            case "Actif" -> "status-actif";
            case "Inactif" -> "status-inactif";
            case "En attente" -> "status-attente";
            default -> "";
        };
    }
}
