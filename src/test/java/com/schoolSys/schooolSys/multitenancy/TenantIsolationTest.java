package com.schoolSys.schooolSys.multitenancy;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.common.multitenancy.TenantConnectionProvider;
import com.schoolSys.schooolSys.common.multitenancy.TenantFilter;
import com.schoolSys.schooolSys.common.multitenancy.TenantIdentifierResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * End-to-end tenant data isolation tests [TEN-008].
 * <p>
 * Verifies that the multi-tenancy infrastructure (TenantContext, TenantFilter,
 * TenantIdentifierResolver, TenantConnectionProvider) correctly isolates tenant
 * data across concurrent threads without cross-contamination.
 * </p>
 * <p>
 * These are pure unit tests — no Spring context or real database is required.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tenant Data Isolation Tests [TEN-008]")
class TenantIsolationTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // =========================================================================
    // 1. TenantContext ThreadLocal isolation with concurrent threads
    // =========================================================================

    @Nested
    @DisplayName("Concurrent TenantContext isolation")
    class ConcurrentTenantContextIsolation {

        @Test
        @DisplayName("should isolate tenant across 10 concurrent threads")
        void shouldIsolateTenantAcrossConcurrentThreads() throws Exception {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            Map<String, String> results = new ConcurrentHashMap<>();
            List<String> errors = Collections.synchronizedList(new ArrayList<>());

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                String tenantId = "school_" + i;
                futures.add(executor.submit(() -> {
                    try {
                        // Synchronize all threads to start together
                        barrier.await(5, TimeUnit.SECONDS);

                        TenantContext.setCurrentTenant(tenantId);

                        // Simulate some work — give other threads a chance to interfere
                        Thread.sleep(50);

                        String observed = TenantContext.getCurrentTenant();
                        results.put(tenantId, observed);

                        if (!tenantId.equals(observed)) {
                            errors.add("Thread for " + tenantId
                                    + " observed wrong tenant: " + observed);
                        }
                    } catch (Exception e) {
                        errors.add("Thread for " + tenantId + " threw: " + e.getMessage());
                    } finally {
                        TenantContext.clear();
                    }
                }));
            }

            // Wait for all futures to complete
            for (Future<?> f : futures) {
                f.get(10, TimeUnit.SECONDS);
            }
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

            // Verify no cross-contamination
            assertThat(errors).as("Cross-tenant contamination detected").isEmpty();
            assertThat(results).hasSize(threadCount);
            for (int i = 0; i < threadCount; i++) {
                String tenantId = "school_" + i;
                assertThat(results.get(tenantId))
                        .as("Thread %s should observe its own tenant", tenantId)
                        .isEqualTo(tenantId);
            }
        }

        @Test
        @DisplayName("child threads should not inherit parent tenant")
        void childThreadShouldNotInheritParentTenant() throws Exception {
            TenantContext.setCurrentTenant("parent_school");

            int childCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(childCount);
            Map<Integer, String> childObserved = new ConcurrentHashMap<>();

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < childCount; i++) {
                int idx = i;
                futures.add(executor.submit(() -> {
                    // Child should see default, not parent's tenant
                    childObserved.put(idx, TenantContext.getCurrentTenant());
                }));
            }

            for (Future<?> f : futures) {
                f.get(5, TimeUnit.SECONDS);
            }
            executor.shutdown();

            // Parent's tenant should be intact
            assertThat(TenantContext.getCurrentTenant()).isEqualTo("parent_school");

            // Every child should have seen the default
            for (int i = 0; i < childCount; i++) {
                assertThat(childObserved.get(i))
                        .as("Child thread %d should see default 'public' schema", i)
                        .isEqualTo(TenantContext.DEFAULT_SCHEMA);
            }
        }
    }

    // =========================================================================
    // 2. TenantFilter routing + cleanup under concurrency
    // =========================================================================

    @Nested
    @DisplayName("TenantFilter concurrent routing and cleanup")
    class TenantFilterConcurrentRoutingAndCleanup {

        @Test
        @DisplayName("should clear tenant after filter completes across concurrent requests")
        void shouldClearTenantAfterFilterCompletes() throws Exception {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            List<String> errors = Collections.synchronizedList(new ArrayList<>());

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                String tenantId = "tenant_" + i;
                futures.add(executor.submit(() -> {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);

                        TenantFilter filter = new TenantFilter();
                        MockHttpServletRequest request = new MockHttpServletRequest();
                        MockHttpServletResponse response = new MockHttpServletResponse();
                        request.addHeader("X-Tenant-ID", tenantId);
                        request.setRequestURI("/api/students");

                        AtomicReference<String> tenantDuringChain = new AtomicReference<>();
                        FilterChain chain = mock(FilterChain.class);
                        doAnswer(inv -> {
                            tenantDuringChain.set(TenantContext.getCurrentTenant());
                            // Simulate processing time
                            Thread.sleep(30);
                            return null;
                        }).when(chain).doFilter(request, response);

                        filter.doFilter(request, response, chain);

                        // During chain execution, tenant should have been correct
                        if (!tenantId.equals(tenantDuringChain.get())) {
                            errors.add("During chain, expected " + tenantId
                                    + " but got " + tenantDuringChain.get());
                        }

                        // After filter completes, context must be cleared
                        String afterFilter = TenantContext.getCurrentTenant();
                        if (!TenantContext.DEFAULT_SCHEMA.equals(afterFilter)) {
                            errors.add("After filter for " + tenantId
                                    + ", context was not cleared: " + afterFilter);
                        }

                    } catch (Exception e) {
                        errors.add("Thread for " + tenantId + " threw: " + e.getMessage());
                    }
                }));
            }

            for (Future<?> f : futures) {
                f.get(10, TimeUnit.SECONDS);
            }
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

            assertThat(errors).as("Filter concurrency errors detected").isEmpty();
        }

        @Test
        @DisplayName("should clear tenant even when filter chain throws under concurrency")
        void shouldClearTenantOnExceptionUnderConcurrency() throws Exception {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<String> errors = Collections.synchronizedList(new ArrayList<>());

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                String tenantId = "error_tenant_" + i;
                futures.add(executor.submit(() -> {
                    try {
                        TenantFilter filter = new TenantFilter();
                        MockHttpServletRequest request = new MockHttpServletRequest();
                        MockHttpServletResponse response = new MockHttpServletResponse();
                        request.addHeader("X-Tenant-ID", tenantId);
                        request.setRequestURI("/api/students");

                        FilterChain chain = mock(FilterChain.class);
                        doThrow(new ServletException("simulated failure"))
                                .when(chain).doFilter(request, response);

                        try {
                            filter.doFilter(request, response, chain);
                        } catch (ServletException ignored) {
                            // expected
                        }

                        // Context must be cleared even after exception
                        String afterException = TenantContext.getCurrentTenant();
                        if (!TenantContext.DEFAULT_SCHEMA.equals(afterException)) {
                            errors.add("After exception for " + tenantId
                                    + ", context was not cleared: " + afterException);
                        }
                    } catch (Exception e) {
                        errors.add("Unexpected error for " + tenantId + ": " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }));
            }

            assertThat(latch.await(10, TimeUnit.SECONDS))
                    .as("All threads should finish within timeout")
                    .isTrue();
            executor.shutdown();

            assertThat(errors)
                    .as("Tenant context must be cleared even when filter chain throws")
                    .isEmpty();
        }
    }

    // =========================================================================
    // 3. Rapid tenant switching
    // =========================================================================

    @Nested
    @DisplayName("Rapid tenant switching")
    class RapidTenantSwitching {

        @Test
        @DisplayName("should handle rapid set/clear cycles in a single thread")
        void shouldHandleRapidTenantSwitching() {
            int iterations = 1_000;

            for (int i = 0; i < iterations; i++) {
                String tenantId = "rapid_school_" + (i % 50);
                TenantContext.setCurrentTenant(tenantId);
                assertThat(TenantContext.getCurrentTenant())
                        .as("Iteration %d: expected %s", i, tenantId)
                        .isEqualTo(tenantId);
                TenantContext.clear();
                assertThat(TenantContext.getCurrentTenant())
                        .as("Iteration %d: should be cleared", i)
                        .isEqualTo(TenantContext.DEFAULT_SCHEMA);
            }
        }

        @Test
        @DisplayName("should handle rapid set/clear cycles across multiple threads")
        void shouldHandleRapidTenantSwitchingConcurrently() throws Exception {
            int threadCount = 10;
            int iterationsPerThread = 500;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            AtomicBoolean failureDetected = new AtomicBoolean(false);
            List<String> errors = Collections.synchronizedList(new ArrayList<>());

            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threadCount; t++) {
                int threadIdx = t;
                futures.add(executor.submit(() -> {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);

                        for (int i = 0; i < iterationsPerThread; i++) {
                            String tenantId = "thread_" + threadIdx + "_iter_" + i;
                            TenantContext.setCurrentTenant(tenantId);

                            String observed = TenantContext.getCurrentTenant();
                            if (!tenantId.equals(observed)) {
                                failureDetected.set(true);
                                errors.add("Thread " + threadIdx + " iter " + i
                                        + ": expected " + tenantId + " but got " + observed);
                                break;
                            }

                            TenantContext.clear();

                            String afterClear = TenantContext.getCurrentTenant();
                            if (!TenantContext.DEFAULT_SCHEMA.equals(afterClear)) {
                                failureDetected.set(true);
                                errors.add("Thread " + threadIdx + " iter " + i
                                        + ": expected 'public' after clear but got " + afterClear);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        failureDetected.set(true);
                        errors.add("Thread " + threadIdx + " threw: " + e.getMessage());
                    }
                }));
            }

            for (Future<?> f : futures) {
                f.get(30, TimeUnit.SECONDS);
            }
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

            assertThat(failureDetected.get())
                    .as("Failures during rapid switching: %s", errors)
                    .isFalse();
        }
    }

    // =========================================================================
    // 4. Full stack isolation: TenantFilter -> TenantIdentifierResolver ->
    //    TenantConnectionProvider
    // =========================================================================

    @Nested
    @DisplayName("Full stack tenant resolution")
    class FullStackTenantResolution {

        @Test
        @DisplayName("TenantFilter -> TenantIdentifierResolver chain should be consistent")
        void filterAndResolverShouldBeConsistent() throws Exception {
            TenantFilter filter = new TenantFilter();
            TenantIdentifierResolver resolver = new TenantIdentifierResolver();

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.addHeader("X-Tenant-ID", "school_alpha");
            request.setRequestURI("/api/students");

            AtomicReference<String> resolvedTenant = new AtomicReference<>();
            FilterChain chain = mock(FilterChain.class);
            doAnswer(inv -> {
                // Simulate what Hibernate would do — ask the resolver
                resolvedTenant.set(resolver.resolveCurrentTenantIdentifier());
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);

            assertThat(resolvedTenant.get()).isEqualTo("school_alpha");
            // After filter, resolver should return default
            assertThat(resolver.resolveCurrentTenantIdentifier())
                    .isEqualTo(TenantContext.DEFAULT_SCHEMA);
        }

        @Test
        @DisplayName("TenantConnectionProvider should set correct schema for resolved tenant")
        void connectionProviderShouldSetCorrectSchema() throws SQLException {
            when(dataSource.getConnection()).thenReturn(connection);

            TenantConnectionProvider provider = new TenantConnectionProvider(dataSource);

            Connection tenantConn = provider.getConnection("school_beta");

            verify(connection).setSchema("school_beta");
            assertThat(tenantConn).isSameAs(connection);
        }

        @Test
        @DisplayName("TenantConnectionProvider should reset schema on release")
        void connectionProviderShouldResetSchemaOnRelease() throws SQLException {
            TenantConnectionProvider provider = new TenantConnectionProvider(dataSource);

            provider.releaseConnection("school_beta", connection);

            verify(connection).setSchema(TenantContext.DEFAULT_SCHEMA);
            verify(connection).close();
        }

        @Test
        @DisplayName("Full stack: concurrent requests route through Filter -> Resolver -> ConnectionProvider")
        void fullStackConcurrentIsolation() throws Exception {
            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            List<String> errors = Collections.synchronizedList(new ArrayList<>());

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                String tenantId = "school_" + i;
                futures.add(executor.submit(() -> {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);

                        // Step 1: Simulate TenantFilter setting context
                        TenantFilter filter = new TenantFilter();
                        TenantIdentifierResolver resolver = new TenantIdentifierResolver();

                        MockHttpServletRequest request = new MockHttpServletRequest();
                        MockHttpServletResponse response = new MockHttpServletResponse();
                        request.addHeader("X-Tenant-ID", tenantId);
                        request.setRequestURI("/api/students");

                        FilterChain chain = mock(FilterChain.class);
                        doAnswer(inv -> {
                            // Step 2: Resolver should return the correct tenant
                            String resolved = resolver.resolveCurrentTenantIdentifier();
                            if (!tenantId.equals(resolved)) {
                                errors.add("Resolver mismatch for " + tenantId
                                        + ": got " + resolved);
                            }

                            // Simulate some work
                            Thread.sleep(20);

                            // Step 3: Verify tenant is still correct after work
                            String afterWork = TenantContext.getCurrentTenant();
                            if (!tenantId.equals(afterWork)) {
                                errors.add("Post-work mismatch for " + tenantId
                                        + ": got " + afterWork);
                            }
                            return null;
                        }).when(chain).doFilter(request, response);

                        filter.doFilter(request, response, chain);

                        // Step 4: After filter, context must be cleared
                        String afterFilter = TenantContext.getCurrentTenant();
                        if (!TenantContext.DEFAULT_SCHEMA.equals(afterFilter)) {
                            errors.add("Post-filter for " + tenantId
                                    + " not cleared: " + afterFilter);
                        }

                    } catch (Exception e) {
                        errors.add("Thread for " + tenantId + " threw: " + e.getMessage());
                    }
                }));
            }

            for (Future<?> f : futures) {
                f.get(15, TimeUnit.SECONDS);
            }
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();

            assertThat(errors)
                    .as("Full-stack concurrent isolation errors")
                    .isEmpty();
        }
    }
}
