package com.schoolSys.schooolSys.integration;

import com.schoolSys.schooolSys.integration.dto.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * General-purpose email sending service for integration layer.
 * Uses JavaMailSender when SMTP is configured, otherwise falls back to logging.
 */
@Service("integrationEmailService")
@Slf4j
public class EmailService {

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.from:noreply@ecolenet.dev}")
    private String fromAddress;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public boolean send(EmailRequest request) {
        if (!mailEnabled || mailSender == null) {
            log.info("=== EMAIL (mode log - SMTP non configure) ===");
            log.info("To: {}, Subject: {}, Body: {} caracteres",
                    request.getTo(), request.getSubject(),
                    request.getBody() != null ? request.getBody().length() : 0);
            log.info("==============================================");
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), true);
            mailSender.send(message);
            log.info("Email envoye avec succes a: {} (sujet: {})", request.getTo(), request.getSubject());
            return true;
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email a {}: {}", request.getTo(), e.getMessage(), e);
            return false;
        }
    }

    public boolean sendTemplate(String templateType, Map<String, String> params) {
        log.info("Envoi email template: {} avec {} parametres", templateType, params.size());

        String to = params.getOrDefault("to", "");
        String subject = params.getOrDefault("subject", "Notification");
        String body = buildTemplateBody(templateType, params);

        EmailRequest request = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .templateType(templateType)
                .build();

        return send(request);
    }

    private String buildTemplateBody(String templateType, Map<String, String> params) {
        return switch (templateType) {
            case "WELCOME" -> "Bienvenue " + params.getOrDefault("name", "") + " dans notre etablissement.";
            case "ABSENCE_ALERT" -> "Alerte absence pour l'eleve " + params.getOrDefault("studentName", "") +
                    ". Nombre d'absences: " + params.getOrDefault("count", "0");
            case "PAYMENT_REMINDER" -> "Rappel de paiement pour " + params.getOrDefault("studentName", "") +
                    ". Montant du: " + params.getOrDefault("amount", "0") + " DH";
            case "GRADE_NOTIFICATION" -> "Les notes du " + params.getOrDefault("trimestre", "") +
                    " sont disponibles pour " + params.getOrDefault("studentName", "");
            default -> "Notification: " + params.getOrDefault("message", "");
        };
    }
}
