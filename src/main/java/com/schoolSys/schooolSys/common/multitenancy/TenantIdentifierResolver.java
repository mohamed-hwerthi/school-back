package com.schoolSys.schooolSys.common.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Hibernate SPI implementation that resolves the current tenant identifier
 * from {@link TenantContext}.
 * <p>
 * Hibernate calls this resolver to determine which PostgreSQL schema
 * should be used for the current database session.
 * </p>
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    /**
     * Returns the current tenant identifier (schema name).
     *
     * @return the schema name from {@link TenantContext}
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getCurrentTenant();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
