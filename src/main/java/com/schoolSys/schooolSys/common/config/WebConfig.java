package com.schoolSys.schooolSys.common.config;

import com.schoolSys.schooolSys.common.annee.CurrentAnneeArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    private final CurrentAnneeArgumentResolver currentAnneeArgumentResolver;

    public WebConfig(CurrentAnneeArgumentResolver currentAnneeArgumentResolver) {
        this.currentAnneeArgumentResolver = currentAnneeArgumentResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(86400);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentAnneeArgumentResolver);
    }
}
