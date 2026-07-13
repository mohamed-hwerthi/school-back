package com.schoolSys.schooolSys.common.audit;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Enables Spring Data JPA auditing and resolves the current user id for
 * {@code @CreatedBy} / {@code @LastModifiedBy} on {@link AuditableEntity} (SEC-025).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Supplies the authenticated user id (the JWT subject). Returns empty for
     * anonymous or system writes, leaving the audit column null.
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(UUID.fromString(auth.getName()));
            } catch (NumberFormatException notANumericUserId) {
                return Optional.empty();
            }
        };
    }
}
