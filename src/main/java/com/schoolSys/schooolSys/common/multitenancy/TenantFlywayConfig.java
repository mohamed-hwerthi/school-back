package com.schoolSys.schooolSys.common.multitenancy;

import com.schoolSys.schooolSys.tenant.Tenant;
import com.schoolSys.schooolSys.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * Runs Flyway migrations for all registered tenant schemas.
 * <p>
 * On application startup ({@link ApplicationReadyEvent}), this component
 * iterates over every active tenant and applies the tenant-specific
 * migrations located in {@code classpath:db/tenant-migration}.
 * </p>
 * <p>
 * It is also used by the {@link com.schoolSys.schooolSys.tenant.TenantService}
 * when a new school is registered to initialize its schema.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFlywayConfig {

    /**
     * Flyway locations for tenant migrations. Production keeps schema
     * migrations only ({@code classpath:db/tenant-migration}); dev also adds
     * {@code classpath:db/tenant-seed} so new schemas come with demo data.
     */
    @Value("${app.tenant.flyway-locations:classpath:db/tenant-migration}")
    private String[] tenantMigrationLocations;

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    /**
     * Migrates all existing tenant schemas on application startup.
     * Runs after Spring Boot's default Flyway has migrated the public schema.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void migrateAllTenantSchemas() {
        List<Tenant> tenants = tenantRepository.findAll();
        log.info("Running Flyway migrations for {} tenant schema(s)", tenants.size());

        for (Tenant tenant : tenants) {
            if (tenant.isActive()) {
                migrateTenantSchema(tenant.getSchemaName());
            }
        }
    }

    /**
     * Runs Flyway migrations for a single tenant schema.
     * Creates the schema if it does not exist, then applies all
     * pending migrations.
     *
     * @param schemaName the PostgreSQL schema name
     */
    public void migrateTenantSchema(String schemaName) {
        log.info("Migrating tenant schema: {}", schemaName);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations(tenantMigrationLocations)
                .baselineOnMigrate(true)
                .createSchemas(true)
                .load();

        flyway.migrate();

        log.info("Tenant schema '{}' migrated successfully", schemaName);
    }
}
