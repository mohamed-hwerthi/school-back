package com.schoolSys.schooolSys.multitenancy;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.common.multitenancy.TenantFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("TenantFilter Unit Tests")
class TenantFilterTest {

    private TenantFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new TenantFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Nested
    @DisplayName("doFilterInternal()")
    class DoFilterInternal {

        @Test
        @DisplayName("should set tenant from X-Tenant-ID header")
        void shouldSetTenantFromHeader() throws ServletException, IOException {
            request.addHeader("X-Tenant-ID", "school_xyz");
            request.setRequestURI("/api/students");

            AtomicReference<String> capturedTenant = new AtomicReference<>();
            doAnswer(inv -> {
                capturedTenant.set(TenantContext.getCurrentTenant());
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);

            assertThat(capturedTenant.get()).isEqualTo("school_xyz");
            // After filter completes, context should be cleared
            assertThat(TenantContext.getCurrentTenant()).isEqualTo("public");
        }

        @Test
        @DisplayName("should use default 'public' when header is missing")
        void shouldUseDefaultWhenHeaderMissing() throws ServletException, IOException {
            request.setRequestURI("/api/students");

            AtomicReference<String> capturedTenant = new AtomicReference<>();
            doAnswer(inv -> {
                capturedTenant.set(TenantContext.getCurrentTenant());
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);

            assertThat(capturedTenant.get()).isEqualTo("public");
        }

        @Test
        @DisplayName("should trim whitespace from header value")
        void shouldTrimHeader() throws ServletException, IOException {
            request.addHeader("X-Tenant-ID", "  school_abc  ");
            request.setRequestURI("/api/students");

            AtomicReference<String> capturedTenant = new AtomicReference<>();
            doAnswer(inv -> {
                capturedTenant.set(TenantContext.getCurrentTenant());
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);

            assertThat(capturedTenant.get()).isEqualTo("school_abc");
        }

        @Test
        @DisplayName("should clear context even if filter chain throws")
        void shouldClearContextOnException() throws ServletException, IOException {
            request.addHeader("X-Tenant-ID", "school_xyz");
            request.setRequestURI("/api/students");

            doThrow(new ServletException("boom")).when(chain).doFilter(request, response);

            try {
                filter.doFilter(request, response, chain);
            } catch (ServletException ignored) {}

            assertThat(TenantContext.getCurrentTenant()).isEqualTo("public");
        }
    }

    @Nested
    @DisplayName("Excluded paths — tenant header should be ignored")
    class ExcludedPaths {

        @Test
        @DisplayName("should NOT set tenant for /api/tenants")
        void shouldNotSetTenantForTenantsEndpoint() throws ServletException, IOException {
            request.addHeader("X-Tenant-ID", "school_xyz");
            request.setRequestURI("/api/tenants");

            AtomicReference<String> capturedTenant = new AtomicReference<>();
            doAnswer(inv -> {
                capturedTenant.set(TenantContext.getCurrentTenant());
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);

            // For excluded paths, doFilterInternal is skipped, so tenant stays default
            assertThat(capturedTenant.get()).isEqualTo("public");
        }

        @Test
        @DisplayName("should NOT set tenant for /swagger-ui")
        void shouldNotSetTenantForSwagger() throws ServletException, IOException {
            request.addHeader("X-Tenant-ID", "school_xyz");
            request.setRequestURI("/swagger-ui/index.html");

            AtomicReference<String> capturedTenant = new AtomicReference<>();
            doAnswer(inv -> {
                capturedTenant.set(TenantContext.getCurrentTenant());
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);

            assertThat(capturedTenant.get()).isEqualTo("public");
        }

        @Test
        @DisplayName("should NOT set tenant for /actuator")
        void shouldNotSetTenantForActuator() throws ServletException, IOException {
            request.addHeader("X-Tenant-ID", "school_xyz");
            request.setRequestURI("/actuator/health");

            AtomicReference<String> capturedTenant = new AtomicReference<>();
            doAnswer(inv -> {
                capturedTenant.set(TenantContext.getCurrentTenant());
                return null;
            }).when(chain).doFilter(request, response);

            filter.doFilter(request, response, chain);

            assertThat(capturedTenant.get()).isEqualTo("public");
        }
    }
}
