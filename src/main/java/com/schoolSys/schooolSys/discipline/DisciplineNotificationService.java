package com.schoolSys.schooolSys.discipline;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisciplineNotificationService {

    private final EmailService emailService;

    /**
     * Notify parent about a discipline incident involving their child.
     * The email is sent via EmailService which handles the enabled/disabled flag internally.
     * When SMTP is not configured, the email content is logged instead.
     */
    public void notifyParentIncident(Incident incident, UUID eleveId) {
        log.info("Notification parent pour incident ID: {} (eleve ID: {})", incident.getId(), eleveId);

        // In a full implementation, we would look up the parent's email and student name
        // from the database using eleveId. For now, we construct the notification content
        // and delegate to the email service which handles enabled/disabled state.
        String parentEmail = resolveParentEmail(eleveId);
        String studentName = resolveStudentName(eleveId);

        if (parentEmail == null) {
            log.warn("Impossible de notifier le parent: email introuvable pour l'eleve ID: {}", eleveId);
            return;
        }

        emailService.sendDisciplineIncidentNotification(
                parentEmail,
                studentName,
                incident.getTitre(),
                incident.getType(),
                incident.getGravite(),
                incident.getDate() != null ? incident.getDate().toString() : "N/A",
                incident.getDescription()
        );
    }

    /**
     * Notify parent about a sanction applied to their child.
     * The email is sent via EmailService which handles the enabled/disabled flag internally.
     * When SMTP is not configured, the email content is logged instead.
     */
    public void notifyParentSanction(Sanction sanction) {
        log.info("Notification parent pour sanction ID: {} (eleve ID: {})", sanction.getId(), sanction.getEleveId());

        String parentEmail = resolveParentEmail(sanction.getEleveId());
        String studentName = resolveStudentName(sanction.getEleveId());

        if (parentEmail == null) {
            log.warn("Impossible de notifier le parent: email introuvable pour l'eleve ID: {}", sanction.getEleveId());
            return;
        }

        emailService.sendDisciplineSanctionNotification(
                parentEmail,
                studentName,
                sanction.getType(),
                sanction.getDateDebut() != null ? sanction.getDateDebut().toString() : "N/A",
                sanction.getDateFin() != null ? sanction.getDateFin().toString() : null,
                sanction.getDescription()
        );
    }

    /**
     * Resolve the parent's email for a given student.
     * TODO: Wire up to a ParentRepository or StudentRepository to fetch the real parent email.
     * For now returns a placeholder that will be logged by EmailService when mail is disabled.
     */
    private String resolveParentEmail(UUID eleveId) {
        // Placeholder: in production, query the parent/student relationship table
        log.debug("Resolving parent email for eleve ID: {} (placeholder)", eleveId);
        return "parent-of-eleve-" + eleveId + "@placeholder.local";
    }

    /**
     * Resolve the student's display name for a given student ID.
     * TODO: Wire up to StudentRepository to fetch the real student name.
     */
    private String resolveStudentName(UUID eleveId) {
        log.debug("Resolving student name for eleve ID: {} (placeholder)", eleveId);
        return "Eleve #" + eleveId;
    }
}
