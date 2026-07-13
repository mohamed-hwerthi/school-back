package com.schoolSys.schooolSys.absence;

import com.schoolSys.schooolSys.auth.EmailService;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenceNotificationService {

    private final EmailService emailService;
    private final StudentRepository studentRepository;
    private final AbsenceSettingsRepository settingsRepository;

    /**
     * Notify parent about a student's absence.
     * Only sends if the absence is not justified and notifications are enabled in settings.
     */
    public void notifyParentAbsence(Absence absence) {
        if (Boolean.TRUE.equals(absence.getJustifie())) {
            return;
        }

        AbsenceSettings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElse(AbsenceSettings.builder().build());

        if (!Boolean.TRUE.equals(settings.getNotificationAuto())) {
            log.debug("Auto notification disabled, skipping notification for absence {}", absence.getId());
            return;
        }

        Optional<Student> studentOpt = studentRepository.findById(absence.getEleveId());
        if (studentOpt.isEmpty()) {
            log.warn("Student not found for absence notification: eleveId={}", absence.getEleveId());
            return;
        }

        Student student = studentOpt.get();
        String studentName = student.getFirstName() + " " + student.getLastName();

        if (Boolean.TRUE.equals(settings.getNotificationEmail()) && student.getParentEmail() != null) {
            emailService.sendAbsenceNotification(
                    student.getParentEmail(),
                    studentName,
                    absence.getDate().toString(),
                    absence.getType()
            );
        }

        if (Boolean.TRUE.equals(settings.getNotificationSms()) && student.getParentPhone() != null) {
            log.info("=== SMS NOTIFICATION (simulated) ===");
            log.info("To: {}", student.getParentPhone());
            log.info("Message: Votre enfant {} a ete marque(e) {} le {}",
                    studentName, absence.getType(), absence.getDate());
            log.info("====================================");
        }
    }
}
