package com.schoolSys.schooolSys.importexport;

import java.util.UUID;

import com.opencsv.CSVWriter;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final NoteRepository noteRepository;
    private final PaiementRepository paiementRepository;
    private final AbsenceRepository absenceRepository;

    /**
     * Exports all students to CSV with UTF-8 BOM for Excel compatibility.
     */
    public void exportStudents(OutputStream out) throws IOException {
        // Write UTF-8 BOM
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            // Header
            writer.writeNext(new String[]{
                    "ID", "Matricule", "Nom", "Prenom", "Nom (Arabe)", "Prenom (Arabe)",
                    "Sexe", "Date de naissance", "Lieu de naissance", "Adresse", "Email",
                    "Classe", "Niveau", "Date d'inscription", "Statut",
                    "Nom Parent", "Prenom Parent", "Tel Parent", "Email Parent", "Notes"
            });

            List<Student> students = studentRepository.findAll();
            for (Student s : students) {
                writer.writeNext(new String[]{
                        str(s.getId()), safe(s.getMatricule()), safe(s.getLastName()), safe(s.getFirstName()),
                        safe(s.getLastNameAr()), safe(s.getFirstNameAr()),
                        safe(s.getSex()), str(s.getDateOfBirth()), safe(s.getBirthPlace()), safe(s.getAddress()),
                        safe(s.getEmail()), safe(s.getClasse()), safe(s.getNiveau()),
                        str(s.getEnrollmentDate()), safe(s.getStatus()),
                        safe(s.getParentLastName()), safe(s.getParentFirstName()),
                        safe(s.getParentPhone()), safe(s.getParentEmail()), safe(s.getNotes())
                });
            }
        }
    }

    /**
     * Exports all teachers to CSV.
     */
    public void exportTeachers(OutputStream out) throws IOException {
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "ID", "Nom", "Prenom", "Email", "Specialisation", "Sexe",
                    "Telephone", "Date de naissance", "Date d'embauche", "Statut"
            });

            List<Teacher> teachers = teacherRepository.findAll();
            for (Teacher t : teachers) {
                writer.writeNext(new String[]{
                        str(t.getId()), safe(t.getLastName()), safe(t.getFirstName()),
                        safe(t.getEmail()), safe(t.getSpecialization()), safe(t.getSexe()),
                        safe(t.getTelephone()), str(t.getDateNaissance()),
                        str(t.getDateEmbauche()), safe(t.getStatut())
                });
            }
        }
    }

    /**
     * Exports notes/grades for a specific class and trimester.
     */
    public void exportNotes(UUID classeId, Integer trimestre, OutputStream out) throws IOException {
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "ID", "Eleve ID", "Nom Eleve", "Prenom Eleve",
                    "Examen", "Module", "Trimestre", "Valeur", "Observation"
            });

            List<Note> notes = noteRepository.findByExamenClasseIdAndTrimestre(classeId, trimestre);
            for (Note n : notes) {
                writer.writeNext(new String[]{
                        str(n.getId()),
                        str(n.getStudent() != null ? n.getStudent().getId() : null),
                        n.getStudent() != null ? safe(n.getStudent().getLastName()) : "",
                        n.getStudent() != null ? safe(n.getStudent().getFirstName()) : "",
                        n.getExamen() != null ? safe(n.getExamen().getName()) : "",
                        n.getExamen() != null && n.getExamen().getModule() != null
                                ? safe(n.getExamen().getModule().getName()) : "",
                        str(n.getTrimestre()),
                        str(n.getValeur()),
                        safe(n.getObservation())
                });
            }
        }
    }

    /**
     * Exports payments for a given school year.
     */
    public void exportPaiements(String anneeScolaire, OutputStream out) throws IOException {
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "ID", "Reference", "Eleve ID", "Nom Eleve", "Prenom Eleve",
                    "Type Frais", "Mois", "Annee Scolaire",
                    "Montant Du", "Montant Paye", "Date Paiement",
                    "Mode Paiement", "Statut", "Notes"
            });

            List<Paiement> paiements = paiementRepository.findByAnneeScolaire(anneeScolaire);
            for (Paiement p : paiements) {
                writer.writeNext(new String[]{
                        str(p.getId()), safe(p.getReference()),
                        p.getStudent() != null ? str(p.getStudent().getId()) : "",
                        p.getStudent() != null ? safe(p.getStudent().getLastName()) : "",
                        p.getStudent() != null ? safe(p.getStudent().getFirstName()) : "",
                        p.getTypeFrais() != null ? safe(p.getTypeFrais().getNom()) : "",
                        safe(p.getMois()), safe(p.getAnneeScolaire()),
                        str(p.getMontantDu()), str(p.getMontantPaye()),
                        str(p.getDatePaiement()),
                        p.getModePaiement() != null ? p.getModePaiement().name() : "",
                        p.getStatut() != null ? p.getStatut().name() : "",
                        safe(p.getNotes())
                });
            }
        }
    }

    /**
     * Exports absences for a given date range.
     */
    public void exportAbsences(String from, String to, OutputStream out) throws IOException {
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "ID", "Eleve ID", "Date", "Type", "Seance",
                    "Heure Arrivee", "Justifie", "Motif", "Enseignant ID"
            });

            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);

            List<Absence> absences = absenceRepository.findAll().stream()
                    .filter(a -> !a.getDate().isBefore(fromDate) && !a.getDate().isAfter(toDate))
                    .toList();

            for (Absence a : absences) {
                writer.writeNext(new String[]{
                        str(a.getId()), str(a.getEleveId()), str(a.getDate()),
                        safe(a.getType()), safe(a.getSeance()),
                        a.getHeureArrivee() != null ? a.getHeureArrivee().toString() : "",
                        str(a.getJustifie()), safe(a.getMotif()), str(a.getEnseignantId())
                });
            }
        }
    }

    // --- Template generation ---

    public void writeStudentTemplate(OutputStream out) throws IOException {
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "Nom", "Prenom", "Nom (Arabe)", "Prenom (Arabe)",
                    "Sexe (M/F)", "Date de naissance (YYYY-MM-DD)", "Lieu de naissance",
                    "Adresse", "Email", "Classe", "Niveau", "Statut",
                    "Nom Parent", "Prenom Parent", "Tel Parent", "Email Parent", "Notes"
            });
            // Example row
            writer.writeNext(new String[]{
                    "Benali", "Ahmed", "\u0628\u0646 \u0639\u0644\u064A", "\u0623\u062D\u0645\u062F",
                    "M", "2015-09-15", "Tunis",
                    "10 Rue de la Liberte", "", "1A", "1ere annee", "Actif",
                    "Benali", "Mohamed", "06123456", "parent@email.com", ""
            });
        }
    }

    public void writeTeacherTemplate(OutputStream out) throws IOException {
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "Nom", "Prenom", "Email", "Specialisation", "Sexe (M/F)",
                    "Telephone", "Date de naissance (YYYY-MM-DD)", "Statut"
            });
            writer.writeNext(new String[]{
                    "Bouazizi", "Fatma", "f.bouazizi@school.com", "Mathematiques", "F",
                    "07654321", "1985-03-22", "Actif"
            });
        }
    }

    // --- Utility ---

    private String safe(String val) {
        return val != null ? val : "";
    }

    private String str(Object val) {
        return val != null ? val.toString() : "";
    }
}
