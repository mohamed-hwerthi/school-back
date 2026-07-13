package com.schoolSys.schooolSys.parentnotif;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.examen.Examen;
import com.schoolSys.schooolSys.examen.ExamenRepository;
import com.schoolSys.schooolSys.integration.EmailService;
import com.schoolSys.schooolSys.integration.SmsService;
import com.schoolSys.schooolSys.integration.dto.EmailRequest;
import com.schoolSys.schooolSys.integration.dto.SmsRequest;
import com.schoolSys.schooolSys.note.Note;
import com.schoolSys.schooolSys.note.NoteRepository;
import com.schoolSys.schooolSys.parentnotif.NotificationLog.Channel;
import com.schoolSys.schooolSys.parentnotif.NotificationLog.RecipientType;
import com.schoolSys.schooolSys.parentnotif.NotificationLog.Status;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service métier pour notifier les parents (SMS / Email).
 * Toutes les méthodes loguent systématiquement dans notification_logs avant et
 * après envoi, qu'il y ait succès, échec ou skip.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParentNotificationService {

    private final NotificationLogRepository logRepo;
    private final SmsService smsService;
    @org.springframework.beans.factory.annotation.Qualifier("integrationEmailService")
    private final EmailService emailService;
    private final StudentRepository studentRepository;
    private final NoteRepository noteRepository;
    private final ExamenRepository examenRepository;

    private static final Set<Channel> ALL_CHANNELS = Set.of(Channel.SMS, Channel.EMAIL);

    /**
     * Trigger automatique : note saisie/mise à jour → notifier les parents.
     * Appelée par NoteService après upsertBulk. Async pour ne pas bloquer la requête HTTP.
     */
    @Async
    @Transactional
    public void notifyNoteSaved(UUID noteId) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            log.warn("[notif] Note {} introuvable, skip notification", noteId);
            return;
        }
        // ABSENT/EXEMPT → on n'envoie pas le score 0 comme "vraie note"
        if (!"PRESENT".equals(note.getStatut()) || note.getValeur() == null) return;

        sendNoteAlert(note, ALL_CHANNELS, null);
    }

    /**
     * Trigger manuel via API : envoi pour une note spécifique avec choix du canal.
     */
    @Transactional
    public List<NotificationLog> sendForNote(UUID noteId, Set<Channel> channels, UUID triggeredByUserId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", noteId));
        return sendNoteAlert(note, channels, triggeredByUserId);
    }

    /**
     * Trigger broadcast : pour TOUTES les notes d'un examen → un envoi par parent.
     */
    @Transactional
    public int sendForExamen(UUID examenId, Set<Channel> channels, UUID triggeredByUserId) {
        Examen examen = examenRepository.findById(examenId)
                .orElseThrow(() -> new ResourceNotFoundException("Examen", examenId));
        List<Note> notes = noteRepository.findByExamenIdAndTrimestre(examen.getId(), examen.getTrimestre());
        int count = 0;
        for (Note n : notes) {
            if ("PRESENT".equals(n.getStatut()) && n.getValeur() != null) {
                sendNoteAlert(n, channels, triggeredByUserId);
                count++;
            }
        }
        return count;
    }

    /**
     * Envoi libre (texte custom) à un parent — utilisé par le bouton "Notifier".
     */
    @Transactional
    public List<NotificationLog> sendManualToParent(UUID studentId, String message,
                                                     Set<Channel> channels, UUID triggeredByUserId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        ParentNotificationTemplates.Rendered tpl = ParentNotificationTemplates.manual(message);

        List<NotificationLog> logs = new ArrayList<>();
        for (Channel ch : channels) {
            String addr = addressFor(student, ch);
            if (addr == null || addr.isBlank()) {
                logs.add(persistAndSkip(student, ch, ParentNotificationEvent.MANUAL,
                        tpl.getSubject(), tpl.getBody(), "Adresse parent manquante",
                        null, null, triggeredByUserId));
                continue;
            }
            logs.add(dispatch(student, ch, ParentNotificationEvent.MANUAL, tpl, addr,
                    "STUDENT", studentId, triggeredByUserId));
        }
        return logs;
    }

    // ───────────────────────────────────── core dispatch ─────────────────────────────────────

    private List<NotificationLog> sendNoteAlert(Note note, Set<Channel> channels, UUID triggeredByUserId) {
        Student student = note.getStudent();
        String moduleName = note.getExamen() != null && note.getExamen().getModule() != null
                ? note.getExamen().getModule().getName() : null;
        String examName = note.getExamen() != null ? note.getExamen().getName() : null;

        ParentNotificationTemplates.Rendered tpl = ParentNotificationTemplates.notePubliee(
                student.getFirstName(), moduleName, examName, note.getValeur(), note.getTrimestre());

        List<NotificationLog> logs = new ArrayList<>();
        for (Channel ch : channels) {
            String addr = addressFor(student, ch);
            if (addr == null || addr.isBlank()) {
                logs.add(persistAndSkip(student, ch, ParentNotificationEvent.NOTE_PUBLIEE,
                        tpl.getSubject(), tpl.getBody(), "Adresse parent manquante",
                        "NOTE", note.getId(), triggeredByUserId));
                continue;
            }
            logs.add(dispatch(student, ch, ParentNotificationEvent.NOTE_PUBLIEE, tpl, addr,
                    "NOTE", note.getId(), triggeredByUserId));
        }
        return logs;
    }

    private NotificationLog dispatch(Student student, Channel channel,
                                      ParentNotificationEvent event,
                                      ParentNotificationTemplates.Rendered tpl,
                                      String address,
                                      String relatedType, UUID relatedId,
                                      UUID triggeredByUserId) {
        // 1. Log PENDING
        NotificationLog row = logRepo.save(NotificationLog.builder()
                .recipientType(RecipientType.PARENT)
                .recipientId(student.getId())
                .recipientAddress(address)
                .channel(channel)
                .eventType(event)
                .subject(tpl.getSubject())
                .body(tpl.getBody())
                .status(Status.PENDING)
                .relatedEntityType(relatedType)
                .relatedEntityId(relatedId)
                .triggeredByUserId(triggeredByUserId)
                .build());

        // 2. Send
        boolean ok;
        String error = null;
        try {
            ok = (channel == Channel.SMS)
                    ? smsService.send(SmsRequest.builder().phoneNumber(address).message(tpl.getBody()).build())
                    : emailService.send(EmailRequest.builder().to(address).subject(tpl.getSubject()).body(tpl.getBody()).build());
        } catch (Exception ex) {
            ok = false;
            error = ex.getMessage();
            log.error("[notif] Échec envoi {} à {}", channel, address, ex);
        }

        // 3. Update log status
        row.setStatus(ok ? Status.SENT : Status.FAILED);
        row.setSentAt(ok ? LocalDateTime.now() : null);
        row.setErrorMessage(error);
        return logRepo.save(row);
    }

    private NotificationLog persistAndSkip(Student student, Channel channel,
                                            ParentNotificationEvent event,
                                            String subject, String body, String reason,
                                            String relatedType, UUID relatedId,
                                            UUID triggeredByUserId) {
        return logRepo.save(NotificationLog.builder()
                .recipientType(RecipientType.PARENT)
                .recipientId(student.getId())
                .recipientAddress("")
                .channel(channel)
                .eventType(event)
                .subject(subject)
                .body(body)
                .status(Status.SKIPPED)
                .errorMessage(reason)
                .relatedEntityType(relatedType)
                .relatedEntityId(relatedId)
                .triggeredByUserId(triggeredByUserId)
                .build());
    }

    private String addressFor(Student student, Channel channel) {
        return switch (channel) {
            case SMS -> student.getParentPhone();
            case EMAIL -> student.getParentEmail();
            case PUSH -> null;
        };
    }
}
