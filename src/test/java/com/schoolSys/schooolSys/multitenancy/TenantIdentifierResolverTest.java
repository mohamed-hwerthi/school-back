package com.schoolSys.schooolSys.multitenancy;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.common.multitenancy.TenantIdentifierResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TenantIdentifierResolver Unit Tests")
class TenantIdentifierResolverTest {

    private final TenantIdentifierResolver resolver = new TenantIdentifierResolver();

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should resolve current tenant from TenantContext")
    void shouldResolveCurrentTenant() {
        TenantContext.setCurrentTenant("school_demo");
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo("school_demo");
    }

    @Test
    @DisplayName("should resolve 'public' when no tenant is set")
    void shouldResolvePublicByDefault() {
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo("public");
    }

    @Test
    @DisplayName("should validate existing sessions")
    void shouldValidateExistingSessions() {
        assertThat(resolver.validateExistingCurrentSessions()).isTrue();
    }
}
