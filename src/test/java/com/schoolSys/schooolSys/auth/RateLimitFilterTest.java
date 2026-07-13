package com.schoolSys.schooolSys.auth;

import com.schoolSys.schooolSys.common.config.RateLimitFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("RateLimitFilter Tests")
class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        chain = mock(FilterChain.class);
    }

    private MockHttpServletRequest postRequest(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
        req.setRequestURI(uri);
        req.setRemoteAddr("192.168.1.100");
        return req;
    }

    private MockHttpServletRequest getRequest(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRequestURI(uri);
        req.setRemoteAddr("192.168.1.100");
        return req;
    }

    @Nested
    @DisplayName("Login endpoint — 10 req/min limit")
    class LoginLimit {

        @Test
        @DisplayName("should allow first 10 login attempts")
        void shouldAllowFirst10() throws ServletException, IOException {
            for (int i = 0; i < 10; i++) {
                MockHttpServletResponse res = new MockHttpServletResponse();
                filter.doFilter(postRequest("/api/auth/login"), res, chain);
                assertThat(res.getStatus()).isEqualTo(200);
            }
            verify(chain, times(10)).doFilter(any(), any());
        }

        @Test
        @DisplayName("should block 11th login attempt with 429")
        void shouldBlock11th() throws ServletException, IOException {
            for (int i = 0; i < 10; i++) {
                filter.doFilter(postRequest("/api/auth/login"), new MockHttpServletResponse(), chain);
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            filter.doFilter(postRequest("/api/auth/login"), blocked, chain);

            assertThat(blocked.getStatus()).isEqualTo(429);
            assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
            assertThat(blocked.getContentAsString()).contains("Trop de");
            verify(chain, times(10)).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("Forgot password — 5 req/min limit")
    class ForgotPasswordLimit {

        @Test
        @DisplayName("should block after 5 forgot-password requests")
        void shouldBlockAfter5() throws ServletException, IOException {
            for (int i = 0; i < 5; i++) {
                MockHttpServletResponse res = new MockHttpServletResponse();
                filter.doFilter(postRequest("/api/auth/forgot-password"), res, chain);
                assertThat(res.getStatus()).isEqualTo(200);
            }

            MockHttpServletResponse blocked = new MockHttpServletResponse();
            filter.doFilter(postRequest("/api/auth/forgot-password"), blocked, chain);
            assertThat(blocked.getStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("Global endpoints — 120 req/min limit")
    class GlobalLimit {

        @Test
        @DisplayName("should allow many GET requests")
        void shouldAllowManyGets() throws ServletException, IOException {
            for (int i = 0; i < 120; i++) {
                MockHttpServletResponse res = new MockHttpServletResponse();
                filter.doFilter(getRequest("/api/students"), res, chain);
                assertThat(res.getStatus()).isEqualTo(200);
            }
            verify(chain, times(120)).doFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("IP isolation")
    class IpIsolation {

        @Test
        @DisplayName("different IPs should have separate limits")
        void shouldIsolateLimitsPerIp() throws ServletException, IOException {
            // Exhaust limit for IP 1
            for (int i = 0; i < 10; i++) {
                MockHttpServletRequest req = postRequest("/api/auth/login");
                req.setRemoteAddr("10.0.0.1");
                filter.doFilter(req, new MockHttpServletResponse(), chain);
            }

            // IP 2 should still be allowed
            MockHttpServletRequest req2 = postRequest("/api/auth/login");
            req2.setRemoteAddr("10.0.0.2");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req2, res, chain);

            assertThat(res.getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("should use X-Forwarded-For header for client IP")
        void shouldUseXForwardedFor() throws ServletException, IOException {
            // Exhaust limit using X-Forwarded-For IP
            for (int i = 0; i < 10; i++) {
                MockHttpServletRequest req = postRequest("/api/auth/login");
                req.addHeader("X-Forwarded-For", "203.0.113.50, 10.0.0.1");
                filter.doFilter(req, new MockHttpServletResponse(), chain);
            }

            // 11th from same forwarded IP should be blocked
            MockHttpServletRequest blocked = postRequest("/api/auth/login");
            blocked.addHeader("X-Forwarded-For", "203.0.113.50");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(blocked, res, chain);

            assertThat(res.getStatus()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("Response headers")
    class Headers {

        @Test
        @DisplayName("should include rate limit headers")
        void shouldIncludeHeaders() throws ServletException, IOException {
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(postRequest("/api/auth/login"), res, chain);

            assertThat(res.getHeader("X-RateLimit-Limit")).isEqualTo("10");
            assertThat(res.getHeader("X-RateLimit-Remaining")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Excluded paths — should always pass through")
    class ExcludedPaths {

        @Test
        @DisplayName("actuator requests should never be rate limited")
        void shouldNotLimitActuator() throws ServletException, IOException {
            // Even after 200 requests, actuator should pass
            for (int i = 0; i < 200; i++) {
                MockHttpServletResponse res = new MockHttpServletResponse();
                filter.doFilter(getRequest("/actuator/health"), res, chain);
                assertThat(res.getStatus()).isEqualTo(200);
            }
        }
    }
}
