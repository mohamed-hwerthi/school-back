package com.schoolSys.schooolSys.integration;

import com.schoolSys.schooolSys.integration.dto.EmailRequest;
import com.schoolSys.schooolSys.integration.dto.SmsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Central notification dispatcher.
 * Decides which channel(s) to use and delegates to the appropriate service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final WebhookService webhookService;

    public void sendByEmail(String to, String subject, String body) {
        EmailRequest request = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .build();
        emailService.send(request);
    }

    public void sendBySms(String phoneNumber, String message) {
        SmsRequest request = SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .message(message)
                .build();
        smsService.send(request);
    }

    public void sendPush(String event, Map<String, Object> payload) {
        log.info("Push notification (via webhook): event={}", event);
        webhookService.triggerEvent(event, payload);
    }

    public void sendAll(String email, String phone, String subject, String message, String event, Map<String, Object> webhookPayload) {
        log.info("Envoi notification multi-canal: email={}, phone={}, event={}", email, phone, event);

        if (email != null && !email.isEmpty()) {
            sendByEmail(email, subject, message);
        }

        if (phone != null && !phone.isEmpty()) {
            sendBySms(phone, message);
        }

        if (event != null && !event.isEmpty()) {
            sendPush(event, webhookPayload != null ? webhookPayload : Map.of("message", message));
        }
    }
}
