package com.schoolSys.schooolSys.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationConfigDTO {

    private String smtpHost;

    private Integer smtpPort;

    private String smtpUsername;

    private String smtpPassword;

    private String smsProvider;

    private String smsApiKey;

    private List<WebhookConfig> webhookConfigs;
}
