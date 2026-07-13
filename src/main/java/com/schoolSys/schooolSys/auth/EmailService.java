package com.schoolSys.schooolSys.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Value("${app.frontend-url:http://localhost:5000}")
    private String frontendUrl;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.from:noreply@ecolenet.dev}")
    private String fromAddress;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Wraps email content in a branded EcoleNet HTML template with
     * purple gradient header, consistent styling, and footer.
     */
    private String wrapInTemplate(String title, String content) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f4f4f5;font-family:Arial,Helvetica,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" role="presentation" style="max-width:600px;margin:20px auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.08);">
                    <!-- Header -->
                    <tr>
                      <td style="background:linear-gradient(135deg,#7c3aed,#6d28d9);padding:28px 24px;text-align:center;">
                        <div style="display:inline-block;width:44px;height:44px;background:rgba(255,255,255,0.2);border-radius:12px;line-height:44px;font-size:22px;font-weight:900;color:#ffffff;text-align:center;">E</div>
                        <div style="color:#ffffff;font-size:20px;font-weight:700;margin-top:10px;letter-spacing:-0.3px;">EcoleNet</div>
                      </td>
                    </tr>
                    <!-- Body -->
                    <tr>
                      <td style="padding:32px 24px;">
                        <h2 style="margin:0 0 20px;color:#1e293b;font-size:20px;font-weight:700;">%s</h2>
                        %s
                      </td>
                    </tr>
                    <!-- Footer -->
                    <tr>
                      <td style="padding:20px 24px;background:#f8fafc;text-align:center;border-top:1px solid #e2e8f0;">
                        <p style="margin:0 0 4px;font-size:12px;color:#94a3b8;">Ce message a ete envoye automatiquement par EcoleNet.</p>
                        <p style="margin:0;font-size:12px;color:#94a3b8;">Ne repondez pas a cet email.</p>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(title, content);
    }

    /**
     * Send an HTML email. If mail is not enabled or the sender is not configured,
     * falls back to logging the email content.
     */
    public void sendEmail(String to, String subject, String htmlBody) {
        if (!mailEnabled || mailSender == null) {
            log.info("=== EMAIL (mode log - SMTP non configure) ===");
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("Body: {}", htmlBody);
            log.info("==============================================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email envoye avec succes a: {} (sujet: {})", to, subject);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email a {}: {}", to, e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        String subject = "Reinitialisation de votre mot de passe - EcoleNet";
        String content = """
                <p style="color:#475569;line-height:1.6;">Vous avez demande la reinitialisation de votre mot de passe.</p>
                <p style="color:#475569;line-height:1.6;">Cliquez sur le bouton ci-dessous pour definir un nouveau mot de passe :</p>
                <p style="text-align:center;margin:28px 0;">
                  <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#6d28d9);color:#ffffff;padding:12px 28px;text-decoration:none;border-radius:8px;font-weight:600;font-size:14px;">Reinitialiser mon mot de passe</a>
                </p>
                <p style="color:#475569;line-height:1.6;">Ce lien expire dans <strong>1 heure</strong>.</p>
                <p style="color:#94a3b8;font-size:13px;margin-top:20px;">Si vous n'avez pas fait cette demande, ignorez cet email.</p>
                """.formatted(resetLink);

        sendEmail(toEmail, subject, wrapInTemplate("Reinitialisation de mot de passe", content));
    }

    /**
     * Sends an absence notification email to a parent.
     */
    public void sendAbsenceNotification(String parentEmail, String studentName, String date, String type) {
        String typeLabel = "RETARD".equals(type) ? "retard" : "absence";
        String subject = "Notification d'" + typeLabel + " - " + studentName;
        String content = """
                <p style="color:#475569;line-height:1.6;">Cher(e) parent,</p>
                <p style="color:#475569;line-height:1.6;">Nous vous informons que votre enfant <strong>%s</strong> a ete marque(e) en <strong>%s</strong> le <strong>%s</strong>.</p>
                <p style="text-align:center;margin:28px 0;">
                  <a href="%s/dashboard/absences" style="display:inline-block;background:linear-gradient(135deg,#7c3aed,#6d28d9);color:#ffffff;padding:12px 28px;text-decoration:none;border-radius:8px;font-weight:600;font-size:14px;">Voir les details</a>
                </p>
                """.formatted(studentName, typeLabel, date, frontendUrl);

        sendEmail(parentEmail, subject, wrapInTemplate("Notification d'" + typeLabel, content));
    }

    /**
     * Sends a discipline incident notification email to a parent.
     */
    public void sendDisciplineIncidentNotification(String parentEmail, String studentName,
                                                    String incidentTitle, String incidentType,
                                                    String gravite, String date, String description) {
        String subject = "Incident disciplinaire - " + studentName;
        String content = """
                <p style="color:#475569;line-height:1.6;">Cher(e) parent,</p>
                <p style="color:#475569;line-height:1.6;">Nous vous informons qu'un incident disciplinaire a ete enregistre concernant votre enfant <strong>%s</strong>.</p>
                <table style="border-collapse:collapse;width:100%%;margin:20px 0;border-radius:8px;overflow:hidden;">
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Titre</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Type</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Gravite</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Date</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Description</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                </table>
                <p style="color:#475569;line-height:1.6;">Nous vous invitons a prendre contact avec l'etablissement pour en discuter.</p>
                """.formatted(studentName, incidentTitle, incidentType, gravite, date,
                description != null ? description : "N/A");

        sendEmail(parentEmail, subject, wrapInTemplate("Notification d'incident disciplinaire", content));
    }

    /**
     * Sends a discipline sanction notification email to a parent.
     */
    public void sendDisciplineSanctionNotification(String parentEmail, String studentName,
                                                    String sanctionType, String dateDebut,
                                                    String dateFin, String description) {
        String subject = "Sanction disciplinaire - " + studentName;
        String content = """
                <p style="color:#475569;line-height:1.6;">Cher(e) parent,</p>
                <p style="color:#475569;line-height:1.6;">Nous vous informons qu'une sanction disciplinaire a ete appliquee a votre enfant <strong>%s</strong>.</p>
                <table style="border-collapse:collapse;width:100%%;margin:20px 0;border-radius:8px;overflow:hidden;">
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Type de sanction</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Date de debut</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Date de fin</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                    <tr><td style="padding:10px 14px;border:1px solid #e2e8f0;font-weight:600;color:#334155;background:#f8fafc;">Description</td>
                        <td style="padding:10px 14px;border:1px solid #e2e8f0;color:#475569;">%s</td></tr>
                </table>
                <p style="color:#475569;line-height:1.6;">Nous vous invitons a prendre contact avec l'etablissement pour en discuter.</p>
                """.formatted(studentName, sanctionType, dateDebut,
                dateFin != null ? dateFin : "Non definie",
                description != null ? description : "N/A");

        sendEmail(parentEmail, subject, wrapInTemplate("Notification de sanction disciplinaire", content));
    }
}
