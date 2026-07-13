package com.schoolSys.schooolSys.common.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hibernate SPI implementation that provides JDBC connections
 * with the correct PostgreSQL schema set.
 * <p>
 * When Hibernate requests a connection for a specific tenant,
 * this provider obtains a connection from the shared {@link DataSource}
 * and sets the schema via {@link Connection#setSchema(String)}.
 * </p>
 */
@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    /**
     * @param dataSource the shared application datasource
     */
    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns a connection without any tenant-specific schema set.
     *
     * @return a JDBC connection from the shared datasource
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Releases a non-tenant-specific connection.
     *
     * @param connection the connection to release
     */
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    /**
     * Returns a JDBC connection with the schema set to the given tenant.
     *
     * @param tenantIdentifier the PostgreSQL schema name for the school
     * @return a connection configured for the tenant's schema
     */
    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        connection.setSchema(tenantIdentifier);
        return connection;
    }

    /**
     * Resets the schema back to the default and releases the connection.
     *
     * @param tenantIdentifier the tenant that was using the connection
     * @param connection       the connection to release
     */
    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        connection.setSchema(TenantContext.DEFAULT_SCHEMA);
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Cannot unwrap " + unwrapType);
    }
}
