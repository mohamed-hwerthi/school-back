package com.schoolSys.schooolSys.auth;

import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.schoolSys.schooolSys.common.security.AuditingAccessDeniedHandler;
import com.schoolSys.schooolSys.common.security.RestAuthenticationEntryPoint;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuditingAccessDeniedHandler.class, RestAuthenticationEntryPoint.class, SecurityTest.JacksonTestConfig.class})
@DisplayName("Security Configuration Tests")
class SecurityTest {

    @TestConfiguration
    static class JacksonTestConfig {
        @Bean
        ObjectMapper objectMapper() { return new ObjectMapper(); }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private CurrentUserContext currentUserContext;

    @MockitoBean
    private CorsConfigurationSource corsConfigurationSource;

    @MockitoBean
    private AuditService auditService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { inv.<jakarta.servlet.FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("POST /api/auth/login should be accessible without auth")
        void loginShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/api/auth/login"))
                    // login is POST-only, but the point is it should not return 401
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        // Should not be 401 (Unauthorized) - may be 405 (method not allowed) since login is POST
                        assert statusCode != 401 : "Expected non-401 status, got " + statusCode;
                    });
        }

        @Test
        @DisplayName("Swagger UI should be accessible without auth")
        void swaggerShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "Expected non-401 status for swagger, got " + statusCode;
                    });
        }

        @Test
        @DisplayName("Actuator endpoints should be accessible without auth")
        void actuatorShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "Expected non-401 status for actuator, got " + statusCode;
                    });
        }
    }

    @Nested
    @DisplayName("Protected Endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("GET /api/students should require authentication")
        void studentsShouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/students"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/paiements should require authentication")
        void paiementsShouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/paiements"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/auth/me should require authentication")
        void meShouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Role-Based Access")
    class RoleBasedAccess {

        @Test
        @WithMockUser(authorities = "READ_STUDENTS")
        @DisplayName("should not return 401 with valid auth")
        void shouldNotReturn401WithValidAuth() throws Exception {
            mockMvc.perform(get("/api/students"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "Expected non-401 status with valid auth, got " + statusCode;
                    });
        }
    }
}
