package com.schoolSys.schooolSys.common.multitenancy;

/**
 * Holds the current tenant identifier using a {@link ThreadLocal}.
 * <p>
 * The tenant identifier corresponds to a PostgreSQL schema name.
 * Each incoming request sets the tenant via {@link TenantFilter},
 * and it is cleared after the request completes.
 * </p>
 */
public final class TenantContext {

    /** Default PostgreSQL schema used when no tenant is specified. */
    public static final String DEFAULT_SCHEMA = "public";

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * Returns the current tenant identifier, or {@value #DEFAULT_SCHEMA} if none is set.
     *
     * @return the current tenant schema name
     */
    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : DEFAULT_SCHEMA;
    }

    /**
     * Sets the current tenant identifier for this thread.
     *
     * @param tenantId the schema name of the school
     */
    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /** Clears the tenant identifier from the current thread. */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
