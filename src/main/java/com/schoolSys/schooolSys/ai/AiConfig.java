package com.schoolSys.schooolSys.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ai")
@Data
public class AiConfig {
    private boolean enabled = false;
    private String provider = "anthropic"; // or "openai"
    private String apiKey = "";
    private String model = "claude-sonnet-4-20250514";
    private int maxTokens = 1024;
}
