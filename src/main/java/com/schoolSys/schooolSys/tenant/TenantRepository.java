package com.schoolSys.schooolSys.tenant;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link Tenant} entities.
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Finds all active schools, ordered by name — used by the public school picker.
     *
     * @return active tenants sorted alphabetically by name
     */
    List<Tenant> findByActiveTrueOrderByNameAsc();

    /**
     * Finds a tenant by its PostgreSQL schema name.
     *
     * @param schemaName the schema name
     * @return the tenant if found
     */
    Optional<Tenant> findBySchemaName(String schemaName);

    /**
     * Checks whether a schema name is already registered.
     *
     * @param schemaName the schema name to check
     * @return {@code true} if it exists
     */
    boolean existsBySchemaName(String schemaName);

    /**
     * Finds an active tenant by its public slug.
     *
     * @param slug the URL-friendly slug
     * @return the tenant if found
     */
    Optional<Tenant> findBySlugAndActiveTrue(String slug);
}
