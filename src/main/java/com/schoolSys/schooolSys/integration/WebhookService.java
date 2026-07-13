package com.schoolSys.schooolSys.integration;

import java.util.UUID;

import com.schoolSys.schooolSys.integration.dto.WebhookConfig;
import com.schoolSys.schooolSys.integration.dto.WebhookEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages webhook registrations and event dispatching.
 * Webhooks are stored in-memory; can be persisted to a database table later.
 */
@Service
@Slf4j
public class WebhookService {

    private final Map<Long, WebhookConfig> webhooks = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);
    private final RestTemplate restTemplate = new RestTemplate();

    public WebhookConfig registerWebhook(WebhookConfig config) {
        Long id = idSequence.getAndIncrement();
        config.setId(id);
        if (config.getEvents() == null) {
            config.setEvents(new ArrayList<>());
        }
        webhooks.put(id, config);
        log.info("Webhook enregistre: id={}, url={}, events={}", id, config.getUrl(), config.getEvents());
        return config;
    }

    public void removeWebhook(Long id) {
        WebhookConfig removed = webhooks.remove(id);
        if (removed != null) {
            log.info("Webhook supprime: id={}, url={}", id, removed.getUrl());
        } else {
            log.warn("Tentative de suppression d'un webhook inexistant: id={}", id);
        }
    }

    public List<WebhookConfig> getAllWebhooks() {
        return new ArrayList<>(webhooks.values());
    }

    public void triggerEvent(String event, Map<String, Object> payload) {
        WebhookEventDTO eventDTO = WebhookEventDTO.builder()
                .event(event)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .build();

        webhooks.values().stream()
                .filter(WebhookConfig::isActive)
                .filter(wh -> wh.getEvents().contains(event) || wh.getEvents().contains("*"))
                .forEach(wh -> dispatchToWebhook(wh, eventDTO));
    }

    private void dispatchToWebhook(WebhookConfig webhook, WebhookEventDTO event) {
        try {
            log.info("Envoi webhook event '{}' vers: {}", event.getEvent(), webhook.getUrl());
            restTemplate.postForEntity(webhook.getUrl(), event, String.class);
            log.info("Webhook event '{}' envoye avec succes a: {}", event.getEvent(), webhook.getUrl());
        } catch (Exception e) {
            log.error("Erreur envoi webhook vers {}: {}", webhook.getUrl(), e.getMessage());
        }
    }
}
