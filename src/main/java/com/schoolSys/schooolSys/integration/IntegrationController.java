package com.schoolSys.schooolSys.integration;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.integration.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final EmailService emailService;
    private final SmsService smsService;
    private final WebhookService webhookService;
    private final IntegrationConfigHolder configHolder;

    @PostMapping("/email/send")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Boolean>> sendEmail(@RequestBody EmailRequest request) {
        boolean result = emailService.send(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/email/test")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Boolean>> sendTestEmail(@RequestBody EmailRequest request) {
        if (request.getSubject() == null || request.getSubject().isEmpty()) {
            request.setSubject("Test Email - School System");
        }
        if (request.getBody() == null || request.getBody().isEmpty()) {
            request.setBody("Ceci est un email de test envoye depuis le systeme de gestion scolaire.");
        }
        boolean result = emailService.send(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/sms/send")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Boolean>> sendSms(@RequestBody SmsRequest request) {
        boolean result = smsService.send(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/sms/test")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Boolean>> sendTestSms(@RequestBody SmsRequest request) {
        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            request.setMessage("Ceci est un SMS de test envoye depuis le systeme de gestion scolaire.");
        }
        boolean result = smsService.send(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/sms/bulk")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<java.util.List<String>>> sendBulkSms(@RequestBody BulkSmsRequest request) {
        java.util.List<String> successfulNumbers = smsService.sendBulk(request);
        return ResponseEntity.ok(ApiResponse.ok(successfulNumbers));
    }

    @GetMapping("/webhooks")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<List<WebhookConfig>>> getWebhooks() {
        return ResponseEntity.ok(ApiResponse.ok(webhookService.getAllWebhooks()));
    }

    @PostMapping("/webhooks")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<WebhookConfig>> createWebhook(@RequestBody WebhookConfig config) {
        return ResponseEntity.ok(ApiResponse.ok(webhookService.registerWebhook(config)));
    }

    @DeleteMapping("/webhooks/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        webhookService.removeWebhook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/config")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<IntegrationConfigDTO>> getConfig() {
        return ResponseEntity.ok(ApiResponse.ok(configHolder.getConfig()));
    }

    @PutMapping("/config")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<IntegrationConfigDTO>> updateConfig(@RequestBody IntegrationConfigDTO config) {
        return ResponseEntity.ok(ApiResponse.ok(configHolder.updateConfig(config)));
    }
}
