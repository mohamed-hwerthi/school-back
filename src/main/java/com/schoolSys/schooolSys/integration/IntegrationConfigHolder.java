package com.schoolSys.schooolSys.integration;

import com.schoolSys.schooolSys.integration.dto.IntegrationConfigDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Holds integration configuration in-memory.
 * Can be extended to persist in database.
 */
@Service
public class IntegrationConfigHolder {

    private IntegrationConfigDTO config = IntegrationConfigDTO.builder()
            .smtpHost("smtp.example.com")
            .smtpPort(587)
            .smtpUsername("")
            .smtpPassword("")
            .smsProvider("")
            .smsApiKey("")
            .webhookConfigs(new ArrayList<>())
            .build();

    public IntegrationConfigDTO getConfig() {
        return config;
    }

    public IntegrationConfigDTO updateConfig(IntegrationConfigDTO newConfig) {
        if (newConfig.getSmtpHost() != null) config.setSmtpHost(newConfig.getSmtpHost());
        if (newConfig.getSmtpPort() != null) config.setSmtpPort(newConfig.getSmtpPort());
        if (newConfig.getSmtpUsername() != null) config.setSmtpUsername(newConfig.getSmtpUsername());
        if (newConfig.getSmtpPassword() != null) config.setSmtpPassword(newConfig.getSmtpPassword());
        if (newConfig.getSmsProvider() != null) config.setSmsProvider(newConfig.getSmsProvider());
        if (newConfig.getSmsApiKey() != null) config.setSmsApiKey(newConfig.getSmsApiKey());
        return config;
    }
}
