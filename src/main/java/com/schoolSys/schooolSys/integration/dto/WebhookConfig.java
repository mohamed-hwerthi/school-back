package com.schoolSys.schooolSys.integration.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookConfig {

    private Long id;

    private String url;

    private List<String> events;

    private String secret;

    private boolean active;
}
