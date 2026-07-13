package com.schoolSys.schooolSys.importexport;

import java.util.UUID;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.schoolSys.schooolSys.importexport.dto.ImportErrorDTO;
import com.schoolSys.schooolSys.importexport.dto.ImportResultDTO;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final NoteRepository noteRepository;

    /**
     * Import students from a CSV file.
     * Expected headers: Nom, Prenom, Nom (Arabe), Prenom (Arabe), Sexe, Date de naissance,
     * Lieu de naissance, Adresse, Email, Classe, Niveau, Statut,
     * Nom Parent, Prenom Parent, Tel Parent, Email Parent, Notes
     */
    @Transactional
    public ImportResultDTO importStudents(InputStream in) {
        ImportResultDTO result = ImportResultDTO.builder()
                .errors(new ArrayList<>())
                .build();

        try (CSVReader reader = new CSVReader(new InputStreamReader(skipBom(in), StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext(); // skip header row
            if (headers == null) {
                result.getErrors().add(ImportErrorDTO.builder()
                        .rowNumber(0).field("file").message("Fichier vide").build());
                return result;
            }

            String[] line;
            int rowNum = 1;
            List<Student> toSave = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                rowNum++;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    if (line.length < 2) {
                        result.getErrors().add(ImportErrorDTO.builder()
                                .rowNumber(rowNum).field("row").message("Ligne incomplete").build());
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    String lastName = getCol(line, 0);
                    String firstName = getCol(line, 1);

                    if (lastName.isBlank() || firstName.isBlank()) {
                        result.getErrors().add(ImportErrorDTO.builder()
                                .rowNumber(rowNum).field("Nom/Prenom").message("Nom et Prenom sont obligatoires").build());
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    Student student = Student.builder()
                            .lastName(lastName)
                            .firstName(firstName)
                            .lastNameAr(getCol(line, 2))
                            .firstNameAr(getCol(line, 3))
                            .sex(getCol(line, 4).isBlank() ? "M" : getCol(line, 4))
                            .dateOfBirth(parseDate(getCol(line, 5)))
                            .birthPlace(getCol(line, 6))
                            .address(getCol(line, 7))
                            .email(getCol(line, 8).isBlank() ? null : getCol(line, 8))
                            .classe(getCol(line, 9))
                            .niveau(getCol(line, 10))
                            .status(getCol(line, 11).isBlank() ? "Actif" : getCol(line, 11))
                            .parentLastName(getCol(line, 12))
                            .parentFirstName(getCol(line, 13))
                            .parentPhone(getCol(line, 14))
                            .parentEmail(getCol(line, 15).isBlank() ? null : getCol(line, 15))
                            .notes(getCol(line, 16))
                            .enrollmentDate(LocalDate.now())
                            .build();

                    toSave.add(student);

                } catch (Exception e) {
                    log.warn("Error importing student at row {}: {}", rowNum, e.getMessage());
                    result.getErrors().add(ImportErrorDTO.builder()
                            .rowNumber(rowNum).field("row").message(e.getMessage()).build());
                    result.setSkipped(result.getSkipped() + 1);
                }
            }

            if (!toSave.isEmpty()) {
                studentRepository.saveAll(toSave);
                result.setImported(toSave.size());
            }

        } catch (IOException | CsvValidationException e) {
            log.error("CSV import error: {}", e.getMessage());
            result.getErrors().add(ImportErrorDTO.builder()
                    .rowNumber(0).field("file").message("Erreur de lecture du fichier: " + e.getMessage()).build());
        }

        return result;
    }

    /**
     * Import teachers from CSV.
     * Expected headers: Nom, Prenom, Email, Specialisation, Sexe, Telephone, Date de naissance, Statut
     */
    @Transactional
    public ImportResultDTO importTeachers(InputStream in) {
        ImportResultDTO result = ImportResultDTO.builder()
                .errors(new ArrayList<>())
                .build();

        try (CSVReader reader = new CSVReader(new InputStreamReader(skipBom(in), StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                result.getErrors().add(ImportErrorDTO.builder()
                        .rowNumber(0).field("file").message("Fichier vide").build());
                return result;
            }

            String[] line;
            int rowNum = 1;
            List<Teacher> toSave = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                rowNum++;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    String lastName = getCol(line, 0);
                    String firstName = getCol(line, 1);

                    if (lastName.isBlank() || firstName.isBlank()) {
                        result.getErrors().add(ImportErrorDTO.builder()
                                .rowNumber(rowNum).field("Nom/Prenom").message("Nom et Prenom sont obligatoires").build());
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    Teacher teacher = Teacher.builder()
                            .lastName(lastName)
                            .firstName(firstName)
                            .email(getCol(line, 2).isBlank() ? null : getCol(line, 2))
                            .specialization(getCol(line, 3))
                            .sexe(getCol(line, 4))
                            .telephone(getCol(line, 5))
                            .dateNaissance(parseDate(getCol(line, 6)))
                            .statut(getCol(line, 7).isBlank() ? "Actif" : getCol(line, 7))
                            .dateEmbauche(LocalDate.now())
                            .build();

                    toSave.add(teacher);

                } catch (Exception e) {
                    log.warn("Error importing teacher at row {}: {}", rowNum, e.getMessage());
                    result.getErrors().add(ImportErrorDTO.builder()
                            .rowNumber(rowNum).field("row").message(e.getMessage()).build());
                    result.setSkipped(result.getSkipped() + 1);
                }
            }

            if (!toSave.isEmpty()) {
                teacherRepository.saveAll(toSave);
                result.setImported(toSave.size());
            }

        } catch (IOException | CsvValidationException e) {
            log.error("CSV import error: {}", e.getMessage());
            result.getErrors().add(ImportErrorDTO.builder()
                    .rowNumber(0).field("file").message("Erreur de lecture du fichier: " + e.getMessage()).build());
        }

        return result;
    }

    /**
     * Import notes from CSV.
     * Expected headers: Student ID, Examen ID, Trimestre, Valeur, Observation
     */
    @Transactional
    public ImportResultDTO importNotes(InputStream in) {
        ImportResultDTO result = ImportResultDTO.builder()
                .errors(new ArrayList<>())
                .build();

        try (CSVReader reader = new CSVReader(new InputStreamReader(skipBom(in), StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext();
            if (headers == null) {
                result.getErrors().add(ImportErrorDTO.builder()
                        .rowNumber(0).field("file").message("Fichier vide").build());
                return result;
            }

            String[] line;
            int rowNum = 1;
            List<Note> toSave = new ArrayList<>();

            while ((line = reader.readNext()) != null) {
                rowNum++;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    String studentIdStr = getCol(line, 0);
                    String examenIdStr = getCol(line, 1);
                    String trimestreStr = getCol(line, 2);
                    String valeurStr = getCol(line, 3);

                    if (studentIdStr.isBlank() || examenIdStr.isBlank() || trimestreStr.isBlank()) {
                        result.getErrors().add(ImportErrorDTO.builder()
                                .rowNumber(rowNum).field("IDs").message("Student ID, Examen ID et Trimestre sont obligatoires").build());
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    UUID studentId = UUID.fromString(studentIdStr);
                    UUID examenId = UUID.fromString(examenIdStr);
                    Integer trimestre = Integer.parseInt(trimestreStr);
                    Double valeur = valeurStr.isBlank() ? null : Double.parseDouble(valeurStr);

                    // Check if note already exists
                    var existing = noteRepository.findByStudentIdAndExamenIdAndTrimestre(studentId, examenId, trimestre);
                    if (existing.isPresent()) {
                        Note note = existing.get();
                        note.setValeur(valeur);
                        note.setObservation(getCol(line, 4));
                        toSave.add(note);
                    } else {
                        Note note = Note.builder()
                                .trimestre(trimestre)
                                .valeur(valeur)
                                .observation(getCol(line, 4))
                                .build();
                        // We need to set student and examen via references
                        var studentRef = studentRepository.findById(studentId);
                        var examenRef = new com.schoolSys.schooolSys.examen.Examen();
                        examenRef.setId(examenId);

                        if (studentRef.isEmpty()) {
                            result.getErrors().add(ImportErrorDTO.builder()
                                    .rowNumber(rowNum).field("studentId").message("Eleve introuvable: " + studentId).build());
                            result.setSkipped(result.getSkipped() + 1);
                            continue;
                        }

                        note.setStudent(studentRef.get());
                        note.setExamen(examenRef);
                        toSave.add(note);
                    }

                } catch (NumberFormatException e) {
                    result.getErrors().add(ImportErrorDTO.builder()
                            .rowNumber(rowNum).field("value").message("Format de nombre invalide: " + e.getMessage()).build());
                    result.setSkipped(result.getSkipped() + 1);
                } catch (Exception e) {
                    log.warn("Error importing note at row {}: {}", rowNum, e.getMessage());
                    result.getErrors().add(ImportErrorDTO.builder()
                            .rowNumber(rowNum).field("row").message(e.getMessage()).build());
                    result.setSkipped(result.getSkipped() + 1);
                }
            }

            if (!toSave.isEmpty()) {
                noteRepository.saveAll(toSave);
                result.setImported(toSave.size());
            }

        } catch (IOException | CsvValidationException e) {
            log.error("CSV import error: {}", e.getMessage());
            result.getErrors().add(ImportErrorDTO.builder()
                    .rowNumber(0).field("file").message("Erreur de lecture du fichier: " + e.getMessage()).build());
        }

        return result;
    }

    // --- Utilities ---

    private String getCol(String[] line, int index) {
        if (index >= line.length) return "";
        return line[index] != null ? line[index].trim() : "";
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Skip UTF-8 BOM if present.
     */
    private InputStream skipBom(InputStream in) throws IOException {
        if (!in.markSupported()) {
            in = new java.io.BufferedInputStream(in);
        }
        in.mark(3);
        byte[] bom = new byte[3];
        int read = in.read(bom);
        if (read < 3 || bom[0] != (byte) 0xEF || bom[1] != (byte) 0xBB || bom[2] != (byte) 0xBF) {
            in.reset();
        }
        return in;
    }
}
