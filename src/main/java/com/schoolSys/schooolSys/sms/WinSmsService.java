package com.schoolSys.schooolSys.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WinSmsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final String baseUrl;
    private final String senderName;

    public WinSmsService(
            @Value("${winsms.api-key:}") String apiKey,
            @Value("${winsms.base-url:https://www.winsms.com/api/v1}") String baseUrl,
            @Value("${winsms.sender-name:SCHOOL}") String senderName) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.senderName = senderName;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean send(String phone, String message) {
        if (!isConfigured()) {
            log.info("[WIN-SMS STUB] To: {}, Message: {}", phone, message);
            return true;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "sender", senderName,
                    "message", message,
                    "recipients", Collections.singletonList(phone)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/sms/send", request, Map.class);

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("[WIN-SMS] SMS sent to {}: {}", phone, response.getBody());
            } else {
                log.error("[WIN-SMS] Failed to send SMS to {}: {}", phone, response.getStatusCode());
            }
            return success;
        } catch (Exception e) {
            log.error("[WIN-SMS] Error sending SMS to {}: {}", phone, e.getMessage());
            return false;
        }
    }

    public boolean sendBulk(List<String> phones, String message) {
        if (!isConfigured()) {
            log.info("[WIN-SMS STUB] Bulk to {} recipients, Message: {}", phones.size(), message);
            return true;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "sender", senderName,
                    "message", message,
                    "recipients", phones
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/sms/send", request, Map.class);

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("[WIN-SMS] Bulk SMS sent to {} recipients: {}", phones.size(), response.getBody());
            } else {
                log.error("[WIN-SMS] Failed to send bulk SMS: {}", response.getStatusCode());
            }
            return success;
        } catch (Exception e) {
            log.error("[WIN-SMS] Error sending bulk SMS: {}", e.getMessage());
            return false;
        }
    }
}
