package com.schoolSys.schooolSys.integration;

import com.schoolSys.schooolSys.integration.dto.BulkSmsRequest;
import com.schoolSys.schooolSys.integration.dto.SmsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SMS sending service with Twilio skeleton.
 * When Twilio credentials are configured, it would use the Twilio API.
 * Otherwise, it falls back to logging the SMS content.
 */
@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.from-number:}")
    private String twilioFromNumber;

    private boolean isTwilioConfigured() {
        return twilioAccountSid != null && !twilioAccountSid.isBlank()
                && twilioAuthToken != null && !twilioAuthToken.isBlank()
                && twilioFromNumber != null && !twilioFromNumber.isBlank();
    }

    public boolean send(SmsRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            log.warn("SMS non envoye: numero de telephone manquant");
            return false;
        }

        if (isTwilioConfigured()) {
            return sendViaTwilio(request.getPhoneNumber(), request.getMessage());
        }

        // Fallback: log the SMS when Twilio is not configured
        log.info("[SMS STUB] Destinataire: {}, Message ({} caracteres): {}",
                request.getPhoneNumber(),
                request.getMessage() != null ? request.getMessage().length() : 0,
                request.getMessage());
        return true;
    }

    /**
     * Send bulk SMS to multiple recipients.
     *
     * @param request contains a list of phone numbers and the message
     * @return list of phone numbers that were successfully sent
     */
    public List<String> sendBulk(BulkSmsRequest request) {
        List<String> successfulNumbers = new ArrayList<>();

        if (request.getPhoneNumbers() == null || request.getPhoneNumbers().isEmpty()) {
            log.warn("SMS en masse: aucun numero fourni");
            return successfulNumbers;
        }

        log.info("Envoi SMS en masse a {} destinataires", request.getPhoneNumbers().size());

        for (String phoneNumber : request.getPhoneNumbers()) {
            try {
                boolean sent = send(SmsRequest.builder()
                        .phoneNumber(phoneNumber)
                        .message(request.getMessage())
                        .build());
                if (sent) {
                    successfulNumbers.add(phoneNumber);
                }
            } catch (Exception e) {
                log.error("Echec envoi SMS au: {}", phoneNumber, e);
            }
        }

        log.info("SMS en masse: {}/{} envoyes avec succes",
                successfulNumbers.size(), request.getPhoneNumbers().size());
        return successfulNumbers;
    }

    /**
     * Sends SMS via Twilio.
     * In production, integrate with Twilio SDK here:
     * <pre>
     *   Twilio.init(twilioAccountSid, twilioAuthToken);
     *   Message.creator(
     *       new PhoneNumber(to),
     *       new PhoneNumber(twilioFromNumber),
     *       body
     *   ).create();
     * </pre>
     */
    private boolean sendViaTwilio(String to, String body) {
        log.info("[TWILIO] Envoi SMS de {} vers {}: {} caracteres",
                twilioFromNumber, to, body != null ? body.length() : 0);

        // TODO: Uncomment when Twilio SDK is added to dependencies
        // try {
        //     Twilio.init(twilioAccountSid, twilioAuthToken);
        //     Message message = Message.creator(
        //             new PhoneNumber(to),
        //             new PhoneNumber(twilioFromNumber),
        //             body
        //     ).create();
        //     log.info("[TWILIO] SMS envoye avec SID: {}", message.getSid());
        //     return true;
        // } catch (Exception e) {
        //     log.error("[TWILIO] Erreur envoi SMS vers {}: {}", to, e.getMessage());
        //     return false;
        // }

        // Stub: return true simulating success
        log.info("[TWILIO] SMS envoye avec succes (skeleton) vers {}", to);
        return true;
    }
}
