package com.schoolSys.schooolSys.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventDTO {

    private String event;

    private Map<String, Object> payload;

    private LocalDateTime timestamp;
}
