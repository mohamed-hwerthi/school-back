package com.schoolSys.schooolSys.importexport;

import java.util.UUID;

import com.schoolSys.schooolSys.absence.Absence;
import com.schoolSys.schooolSys.absence.AbsenceRepository;
import com.schoolSys.schooolSys.finance.Paiement;
import com.schoolSys.schooolSys.finance.PaiementRepository;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final NoteRepository noteRepository;
    private final PaiementRepository paiementRepository;
    private final AbsenceRepository absenceRepository;

    public void exportStudents(OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Eleves");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "ID", "Matricule", "Nom", "Prenom", "Nom (Arabe)", "Prenom (Arabe)",
                    "Sexe", "Date de naissance", "Lieu de naissance", "Adresse", "Email",
                    "Classe", "Niveau", "Date d'inscription", "Statut",
                    "Nom Parent", "Prenom Parent", "Tel Parent", "Email Parent", "Notes"
            };
            createHeaderRow(sheet, headerStyle, headers);

            List<Student> students = studentRepository.findAll();
            int rowNum = 1;
            for (Student s : students) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                setCellValue(row, col++, s.getId());
                setCellValue(row, col++, s.getMatricule());
                setCellValue(row, col++, s.getLastName());
                setCellValue(row, col++, s.getFirstName());
                setCellValue(row, col++, s.getLastNameAr());
                setCellValue(row, col++, s.getFirstNameAr());
                setCellValue(row, col++, s.getSex());
                setCellValue(row, col++, s.getDateOfBirth());
                setCellValue(row, col++, s.getBirthPlace());
                setCellValue(row, col++, s.getAddress());
                setCellValue(row, col++, s.getEmail());
                setCellValue(row, col++, s.getClasse());
                setCellValue(row, col++, s.getNiveau());
                setCellValue(row, col++, s.getEnrollmentDate());
                setCellValue(row, col++, s.getStatus());
                setCellValue(row, col++, s.getParentLastName());
                setCellValue(row, col++, s.getParentFirstName());
                setCellValue(row, col++, s.getParentPhone());
                setCellValue(row, col++, s.getParentEmail());
                setCellValue(row, col, s.getNotes());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        }
    }

    public void exportTeachers(OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Enseignants");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "ID", "Nom", "Prenom", "Email", "Specialisation", "Sexe",
                    "Telephone", "Date de naissance", "Date d'embauche", "Statut"
            };
            createHeaderRow(sheet, headerStyle, headers);

            List<Teacher> teachers = teacherRepository.findAll();
            int rowNum = 1;
            for (Teacher t : teachers) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                setCellValue(row, col++, t.getId());
                setCellValue(row, col++, t.getLastName());
                setCellValue(row, col++, t.getFirstName());
                setCellValue(row, col++, t.getEmail());
                setCellValue(row, col++, t.getSpecialization());
                setCellValue(row, col++, t.getSexe());
                setCellValue(row, col++, t.getTelephone());
                setCellValue(row, col++, t.getDateNaissance());
                setCellValue(row, col++, t.getDateEmbauche());
                setCellValue(row, col, t.getStatut());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        }
    }

    public void exportNotes(UUID classeId, Integer trimestre, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Notes");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "ID", "Eleve ID", "Nom Eleve", "Prenom Eleve",
                    "Examen", "Module", "Trimestre", "Valeur", "Observation"
            };
            createHeaderRow(sheet, headerStyle, headers);

            List<Note> notes = noteRepository.findByExamenClasseIdAndTrimestre(classeId, trimestre);
            int rowNum = 1;
            for (Note n : notes) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                setCellValue(row, col++, n.getId());
                setCellValue(row, col++, n.getStudent() != null ? n.getStudent().getId() : null);
                setCellValue(row, col++, n.getStudent() != null ? n.getStudent().getLastName() : null);
                setCellValue(row, col++, n.getStudent() != null ? n.getStudent().getFirstName() : null);
                setCellValue(row, col++, n.getExamen() != null ? n.getExamen().getName() : null);
                setCellValue(row, col++, n.getExamen() != null && n.getExamen().getModule() != null
                        ? n.getExamen().getModule().getName() : null);
                setCellValue(row, col++, n.getTrimestre());
                setCellValue(row, col++, n.getValeur());
                setCellValue(row, col, n.getObservation());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        }
    }

    public void exportPaiements(String anneeScolaire, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Paiements");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "ID", "Reference", "Eleve ID", "Nom Eleve", "Prenom Eleve",
                    "Type Frais", "Mois", "Annee Scolaire",
                    "Montant Du", "Montant Paye", "Date Paiement",
                    "Mode Paiement", "Statut", "Notes"
            };
            createHeaderRow(sheet, headerStyle, headers);

            List<Paiement> paiements = paiementRepository.findByAnneeScolaire(anneeScolaire);
            int rowNum = 1;
            for (Paiement p : paiements) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                setCellValue(row, col++, p.getId());
                setCellValue(row, col++, p.getReference());
                setCellValue(row, col++, p.getStudent() != null ? p.getStudent().getId() : null);
                setCellValue(row, col++, p.getStudent() != null ? p.getStudent().getLastName() : null);
                setCellValue(row, col++, p.getStudent() != null ? p.getStudent().getFirstName() : null);
                setCellValue(row, col++, p.getTypeFrais() != null ? p.getTypeFrais().getNom() : null);
                setCellValue(row, col++, p.getMois());
                setCellValue(row, col++, p.getAnneeScolaire());
                setCellValue(row, col++, p.getMontantDu() != null ? p.getMontantDu().doubleValue() : null);
                setCellValue(row, col++, p.getMontantPaye() != null ? p.getMontantPaye().doubleValue() : null);
                setCellValue(row, col++, p.getDatePaiement());
                setCellValue(row, col++, p.getModePaiement() != null ? p.getModePaiement().name() : null);
                setCellValue(row, col++, p.getStatut() != null ? p.getStatut().name() : null);
                setCellValue(row, col, p.getNotes());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        }
    }

    public void exportAbsences(String from, String to, OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Absences");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "ID", "Eleve ID", "Date", "Type", "Seance",
                    "Heure Arrivee", "Justifie", "Motif", "Enseignant ID"
            };
            createHeaderRow(sheet, headerStyle, headers);

            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);

            List<Absence> absences = absenceRepository.findAll().stream()
                    .filter(a -> !a.getDate().isBefore(fromDate) && !a.getDate().isAfter(toDate))
                    .toList();

            int rowNum = 1;
            for (Absence a : absences) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                setCellValue(row, col++, a.getId());
                setCellValue(row, col++, a.getEleveId());
                setCellValue(row, col++, a.getDate());
                setCellValue(row, col++, a.getType());
                setCellValue(row, col++, a.getSeance());
                setCellValue(row, col++, a.getHeureArrivee() != null ? a.getHeureArrivee().toString() : null);
                setCellValue(row, col++, a.getJustifie() != null && a.getJustifie() ? "Oui" : "Non");
                setCellValue(row, col++, a.getMotif());
                setCellValue(row, col, a.getEnseignantId());
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        }
    }

    // --- Template generation ---

    public void writeStudentTemplate(OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template Eleves");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Nom", "Prenom", "Nom (Arabe)", "Prenom (Arabe)",
                    "Sexe (M/F)", "Date de naissance (YYYY-MM-DD)", "Lieu de naissance",
                    "Adresse", "Email", "Classe", "Niveau", "Statut",
                    "Nom Parent", "Prenom Parent", "Tel Parent", "Email Parent", "Notes"
            };
            createHeaderRow(sheet, headerStyle, headers);

            Row example = sheet.createRow(1);
            String[] values = {
                    "Benali", "Ahmed", "\u0628\u0646 \u0639\u0644\u064A", "\u0623\u062D\u0645\u062F",
                    "M", "2015-09-15", "Tunis",
                    "10 Rue de la Liberte", "", "1A", "1ere annee", "Actif",
                    "Benali", "Mohamed", "06123456", "parent@email.com", ""
            };
            for (int i = 0; i < values.length; i++) {
                example.createCell(i).setCellValue(values[i]);
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        }
    }

    public void writeTeacherTemplate(OutputStream out) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template Enseignants");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Nom", "Prenom", "Email", "Specialisation", "Sexe (M/F)",
                    "Telephone", "Date de naissance (YYYY-MM-DD)", "Statut"
            };
            createHeaderRow(sheet, headerStyle, headers);

            Row example = sheet.createRow(1);
            String[] values = {
                    "Bouazizi", "Fatma", "f.bouazizi@school.com", "Mathematiques", "F",
                    "07654321", "1985-03-22", "Actif"
            };
            for (int i = 0; i < values.length; i++) {
                example.createCell(i).setCellValue(values[i]);
            }

            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        }
    }

    // --- Helpers ---

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle style, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void setCellValue(Row row, int col, Object value) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number num) {
            cell.setCellValue(num.doubleValue());
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date.toString());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool ? "Oui" : "Non");
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
