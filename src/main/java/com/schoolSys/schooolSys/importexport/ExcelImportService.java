package com.schoolSys.schooolSys.importexport;

import java.util.UUID;

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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public ImportResultDTO importStudents(InputStream in) {
        ImportResultDTO result = ImportResultDTO.builder().errors(new ArrayList<>()).build();

        try (Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.getErrors().add(error(0, "file", "Aucune feuille trouvee"));
                return result;
            }

            List<Student> toSave = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) { // skip header row
                Row row = sheet.getRow(i);
                if (row == null) continue;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    String lastName = getCellStr(row, 0);
                    String firstName = getCellStr(row, 1);

                    if (lastName.isBlank() || firstName.isBlank()) {
                        result.getErrors().add(error(i + 1, "Nom/Prenom", "Nom et Prenom sont obligatoires"));
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    Student student = Student.builder()
                            .lastName(lastName)
                            .firstName(firstName)
                            .lastNameAr(getCellStr(row, 2))
                            .firstNameAr(getCellStr(row, 3))
                            .sex(getCellStr(row, 4).isBlank() ? "M" : getCellStr(row, 4))
                            .dateOfBirth(parseDate(getCellStr(row, 5)))
                            .birthPlace(getCellStr(row, 6))
                            .address(getCellStr(row, 7))
                            .email(getCellStr(row, 8).isBlank() ? null : getCellStr(row, 8))
                            .classe(getCellStr(row, 9))
                            .niveau(getCellStr(row, 10))
                            .status(getCellStr(row, 11).isBlank() ? "Actif" : getCellStr(row, 11))
                            .parentLastName(getCellStr(row, 12))
                            .parentFirstName(getCellStr(row, 13))
                            .parentPhone(getCellStr(row, 14))
                            .parentEmail(getCellStr(row, 15).isBlank() ? null : getCellStr(row, 15))
                            .notes(getCellStr(row, 16))
                            .enrollmentDate(LocalDate.now())
                            .build();

                    toSave.add(student);

                } catch (Exception e) {
                    log.warn("Error importing student at row {}: {}", i + 1, e.getMessage());
                    result.getErrors().add(error(i + 1, "row", e.getMessage()));
                    result.setSkipped(result.getSkipped() + 1);
                }
            }

            if (!toSave.isEmpty()) {
                studentRepository.saveAll(toSave);
                result.setImported(toSave.size());
            }

        } catch (IOException e) {
            log.error("Excel import error: {}", e.getMessage());
            result.getErrors().add(error(0, "file", "Erreur de lecture du fichier: " + e.getMessage()));
        }

        return result;
    }

    @Transactional
    public ImportResultDTO importTeachers(InputStream in) {
        ImportResultDTO result = ImportResultDTO.builder().errors(new ArrayList<>()).build();

        try (Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.getErrors().add(error(0, "file", "Aucune feuille trouvee"));
                return result;
            }

            List<Teacher> toSave = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    String lastName = getCellStr(row, 0);
                    String firstName = getCellStr(row, 1);

                    if (lastName.isBlank() || firstName.isBlank()) {
                        result.getErrors().add(error(i + 1, "Nom/Prenom", "Nom et Prenom sont obligatoires"));
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    Teacher teacher = Teacher.builder()
                            .lastName(lastName)
                            .firstName(firstName)
                            .email(getCellStr(row, 2).isBlank() ? null : getCellStr(row, 2))
                            .specialization(getCellStr(row, 3))
                            .sexe(getCellStr(row, 4))
                            .telephone(getCellStr(row, 5))
                            .dateNaissance(parseDate(getCellStr(row, 6)))
                            .statut(getCellStr(row, 7).isBlank() ? "Actif" : getCellStr(row, 7))
                            .dateEmbauche(LocalDate.now())
                            .build();

                    toSave.add(teacher);

                } catch (Exception e) {
                    log.warn("Error importing teacher at row {}: {}", i + 1, e.getMessage());
                    result.getErrors().add(error(i + 1, "row", e.getMessage()));
                    result.setSkipped(result.getSkipped() + 1);
                }
            }

            if (!toSave.isEmpty()) {
                teacherRepository.saveAll(toSave);
                result.setImported(toSave.size());
            }

        } catch (IOException e) {
            log.error("Excel import error: {}", e.getMessage());
            result.getErrors().add(error(0, "file", "Erreur de lecture du fichier: " + e.getMessage()));
        }

        return result;
    }

    @Transactional
    public ImportResultDTO importNotes(InputStream in) {
        ImportResultDTO result = ImportResultDTO.builder().errors(new ArrayList<>()).build();

        try (Workbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.getErrors().add(error(0, "file", "Aucune feuille trouvee"));
                return result;
            }

            List<Note> toSave = new ArrayList<>();
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    String studentIdStr = getCellStr(row, 0);
                    String examenIdStr = getCellStr(row, 1);
                    String trimestreStr = getCellStr(row, 2);
                    String valeurStr = getCellStr(row, 3);

                    if (studentIdStr.isBlank() || examenIdStr.isBlank() || trimestreStr.isBlank()) {
                        result.getErrors().add(error(i + 1, "IDs", "Student ID, Examen ID et Trimestre sont obligatoires"));
                        result.setSkipped(result.getSkipped() + 1);
                        continue;
                    }

                    UUID studentId = UUID.fromString(studentIdStr.replace(".0", ""));
                    UUID examenId = UUID.fromString(examenIdStr.replace(".0", ""));
                    Integer trimestre = Integer.parseInt(trimestreStr.replace(".0", ""));
                    Double valeur = valeurStr.isBlank() ? null : Double.parseDouble(valeurStr);

                    var existing = noteRepository.findByStudentIdAndExamenIdAndTrimestre(studentId, examenId, trimestre);
                    if (existing.isPresent()) {
                        Note note = existing.get();
                        note.setValeur(valeur);
                        note.setObservation(getCellStr(row, 4));
                        toSave.add(note);
                    } else {
                        var studentRef = studentRepository.findById(studentId);
                        if (studentRef.isEmpty()) {
                            result.getErrors().add(error(i + 1, "studentId", "Eleve introuvable: " + studentId));
                            result.setSkipped(result.getSkipped() + 1);
                            continue;
                        }

                        var examenRef = new com.schoolSys.schooolSys.examen.Examen();
                        examenRef.setId(examenId);

                        Note note = Note.builder()
                                .student(studentRef.get())
                                .examen(examenRef)
                                .trimestre(trimestre)
                                .valeur(valeur)
                                .observation(getCellStr(row, 4))
                                .build();
                        toSave.add(note);
                    }

                } catch (NumberFormatException e) {
                    result.getErrors().add(error(i + 1, "value", "Format de nombre invalide: " + e.getMessage()));
                    result.setSkipped(result.getSkipped() + 1);
                } catch (Exception e) {
                    log.warn("Error importing note at row {}: {}", i + 1, e.getMessage());
                    result.getErrors().add(error(i + 1, "row", e.getMessage()));
                    result.setSkipped(result.getSkipped() + 1);
                }
            }

            if (!toSave.isEmpty()) {
                noteRepository.saveAll(toSave);
                result.setImported(toSave.size());
            }

        } catch (IOException e) {
            log.error("Excel import error: {}", e.getMessage());
            result.getErrors().add(error(0, "file", "Erreur de lecture du fichier: " + e.getMessage()));
        }

        return result;
    }

    // --- Utilities ---

    private String getCellStr(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    yield String.valueOf((long) d);
                }
                yield String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue();
            default -> "";
        };
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private ImportErrorDTO error(int row, String field, String message) {
        return ImportErrorDTO.builder()
                .rowNumber(row).field(field).message(message)
                .build();
    }
}
