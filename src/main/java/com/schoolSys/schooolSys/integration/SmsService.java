package com.schoolSys.schooolSys.integration;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.integration.dto.BulkSmsRequest;
import com.schoolSys.schooolSys.integration.dto.SmsRequest;
import com.schoolSys.schooolSys.sms.SmsCreditService;
import com.schoolSys.schooolSys.sms.WinSmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    private final WinSmsService winSmsService;
    private final SmsCreditService smsCreditService;

    public boolean send(SmsRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            log.warn("SMS non envoye: numero de telephone manquant");
            return false;
        }

        String tenantId = TenantContext.getCurrentTenant();

        if (!smsCreditService.checkAndDeduct(tenantId, 1)) {
            log.warn("Credits SMS insuffisants pour le tenant '{}'", tenantId);
            return false;
        }

        boolean sent = winSmsService.send(request.getPhoneNumber(), request.getMessage());

        if (!sent) {
            smsCreditService.addCredits(tenantId, 1);
            log.warn("Credits SMS rembourses pour le tenant '{}' (envoi echoue)", tenantId);
        }

        return sent;
    }

    public List<String> sendBulk(BulkSmsRequest request) {
        List<String> successfulNumbers = new ArrayList<>();

        if (request.getPhoneNumbers() == null || request.getPhoneNumbers().isEmpty()) {
            log.warn("SMS en masse: aucun numero fourni");
            return successfulNumbers;
        }

        String tenantId = TenantContext.getCurrentTenant();
        int count = request.getPhoneNumbers().size();

        if (!smsCreditService.checkAndDeduct(tenantId, count)) {
            log.warn("Credits SMS insuffisants pour le tenant '{}' (besoin: {}, demande: {})",
                    tenantId, count, smsCreditService.getRemaining(tenantId));
            return successfulNumbers;
        }

        log.info("Envoi SMS en masse a {} destinataires", count);

        boolean allSent = winSmsService.sendBulk(request.getPhoneNumbers(), request.getMessage());

        if (allSent) {
            successfulNumbers.addAll(request.getPhoneNumbers());
        } else {
            for (String phoneNumber : request.getPhoneNumbers()) {
                try {
                    boolean sent = winSmsService.send(phoneNumber, request.getMessage());
                    if (sent) {
                        successfulNumbers.add(phoneNumber);
                    }
                } catch (Exception e) {
                    log.error("Echec envoi SMS au: {}", phoneNumber, e);
                }
            }
        }

        int failed = count - successfulNumbers.size();
        if (failed > 0) {
            smsCreditService.addCredits(tenantId, failed);
            log.warn("{} credits SMS rembourses pour le tenant '{}' (envois echoues)", failed, tenantId);
        }

        log.info("SMS en masse: {}/{} envoyes avec succes", successfulNumbers.size(), count);
        return successfulNumbers;
    }
}
