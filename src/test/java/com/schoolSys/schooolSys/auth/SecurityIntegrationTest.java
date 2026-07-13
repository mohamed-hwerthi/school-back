package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolSys.schooolSys.auth.dto.LoginRequestDTO;
import com.schoolSys.schooolSys.auth.dto.LoginResponseDTO;
import com.schoolSys.schooolSys.auth.dto.RefreshTokenRequestDTO;
import com.schoolSys.schooolSys.auth.dto.UserResponseDTO;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.schoolSys.schooolSys.common.security.AuditingAccessDeniedHandler;
import com.schoolSys.schooolSys.common.security.RestAuthenticationEntryPoint;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end security integration tests verifying that the SecurityConfig
 * correctly protects all routes and allows public endpoints.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuditingAccessDeniedHandler.class, RestAuthenticationEntryPoint.class, SecurityIntegrationTest.JacksonTestConfig.class})
@DisplayName("Security Integration Tests - End-to-End Route Protection")
class SecurityIntegrationTest {

    @TestConfiguration
    static class JacksonTestConfig {
        @Bean
        ObjectMapper objectMapper() { return new ObjectMapper(); }
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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

    private LoginResponseDTO loginResponse;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { inv.<jakarta.servlet.FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(new UUID(0, 1))
                .email("admin@school.com")
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .tenantId("school1")
                .isActive(true)
                .build();

        loginResponse = LoginResponseDTO.builder()
                .accessToken("jwt-access-token")
                .refreshToken("uuid-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .user(userResponse)
                .build();
    }

    // =========================================================================
    // Public Auth Endpoints
    // =========================================================================

    @Nested
    @DisplayName("Public Auth Endpoints (no auth required)")
    class PublicAuthEndpoints {

        @Test
        @DisplayName("POST /api/auth/login should be accessible without auth")
        void loginShouldBeAccessibleWithoutAuth() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("admin@school.com");
            request.setPassword("password123");

            when(authService.login(any(LoginRequestDTO.class), anyString(), anyString(), any()))
                    .thenReturn(loginResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"));
        }

        @Test
        @DisplayName("POST /api/auth/refresh-token should be accessible without auth")
        void refreshTokenShouldBeAccessibleWithoutAuth() throws Exception {
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("valid-token");

            when(authService.refreshToken(any(RefreshTokenRequestDTO.class), anyString(), anyString()))
                    .thenReturn(loginResponse);

            mockMvc.perform(post("/api/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/auth/logout requires authentication (not a public path)")
        void logoutRequiresAuthentication() throws Exception {
            // /api/auth/logout is deliberately NOT in SecurityConfig.PUBLIC_PATHS.
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("token-to-revoke");

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/auth/forgot-password should be accessible without auth")
        void forgotPasswordShouldBeAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"user@school.com\"}"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "forgot-password should not return 401, got " + statusCode;
                        assert statusCode != 403 : "forgot-password should not return 403, got " + statusCode;
                    });
        }

        @Test
        @DisplayName("POST /api/auth/reset-password should be accessible without auth")
        void resetPasswordShouldBeAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"reset-token\",\"newPassword\":\"newPass123\"}"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "reset-password should not return 401, got " + statusCode;
                        assert statusCode != 403 : "reset-password should not return 403, got " + statusCode;
                    });
        }

        @Test
        @DisplayName("POST /api/auth/2fa/verify should be accessible without auth")
        void twoFactorVerifyShouldBeAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/auth/2fa/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":1,\"code\":\"123456\"}"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "2fa/verify should not return 401, got " + statusCode;
                        assert statusCode != 403 : "2fa/verify should not return 403, got " + statusCode;
                    });
        }
    }

    // =========================================================================
    // Public Infra Endpoints
    // =========================================================================

    @Nested
    @DisplayName("Public Infrastructure Endpoints")
    class PublicInfraEndpoints {

        @Test
        @DisplayName("Swagger UI should be accessible without auth")
        void swaggerShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "swagger should not return 401, got " + statusCode;
                    });
        }

        @Test
        @DisplayName("OpenAPI docs should be accessible without auth")
        void openApiDocsShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "api-docs should not return 401, got " + statusCode;
                    });
        }

        @Test
        @DisplayName("Actuator health should be accessible without auth")
        void actuatorHealthShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "actuator should not return 401, got " + statusCode;
                    });
        }

        @Test
        @DisplayName("Public API namespace should be accessible without auth")
        void publicApiShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/api/public/inscriptions/numero/INS-2026-TEST"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "public API should not return 401, got " + statusCode;
                    });
        }
    }

    // =========================================================================
    // Protected Endpoints — Must require authentication
    // =========================================================================

    @Nested
    @DisplayName("Protected Endpoints - Require Authentication")
    class ProtectedEndpoints {

        @Test
        @DisplayName("GET /api/auth/me should require authentication")
        void meShouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isUnauthorized());
        }

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
        @DisplayName("GET /api/emploi-du-temps/classe/1 should require authentication")
        void emploiDuTempsShouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/emploi-du-temps/classe/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/inscriptions should require authentication")
        void inscriptionsShouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/inscriptions"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/auth/2fa/enable should require authentication")
        void twoFactorEnableShouldRequireAuth() throws Exception {
            mockMvc.perform(post("/api/auth/2fa/enable"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/auth/sessions should require authentication")
        void sessionsShouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/auth/sessions"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /api/auth/sessions/1 should require authentication")
        void deleteSessionShouldRequireAuth() throws Exception {
            mockMvc.perform(delete("/api/auth/sessions/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // Role-Based Access Control
    // =========================================================================

    @Nested
    @DisplayName("Role-Based Access Control")
    class RoleBasedAccess {

        @Test
        @WithMockUser(authorities = "READ_STUDENTS")
        @DisplayName("authenticated user should not get 401 for protected endpoints")
        void shouldNotGet401WithValidAuth() throws Exception {
            // Note: these URLs have no handler in @WebMvcTest(AuthController.class),
            // but authenticated users should not get 401
            mockMvc.perform(get("/api/students"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "Should not get 401 with valid auth, got " + statusCode;
                    });
        }

        @Test
        @WithMockUser(authorities = "READ_STUDENTS")
        @DisplayName("authenticated user without correct authority should get 403 for auth/me")
        void authenticatedUserCanAccessMe() throws Exception {
            // /api/auth/me only requires authenticated(), not a specific authority
            when(authService.getCurrentUser(any())).thenReturn(
                    UserResponseDTO.builder()
                            .id(new UUID(0, 1))
                            .email("admin@school.com")
                            .role(UserRole.ADMIN)
                            .build()
            );

            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(result -> {
                        int statusCode = result.getResponse().getStatus();
                        assert statusCode != 401 : "Authenticated user should not get 401, got " + statusCode;
                    });
        }
    }

    // =========================================================================
    // Auth Flow - Login → Refresh → Logout
    // =========================================================================

    @Nested
    @DisplayName("Complete Auth Flow")
    class CompleteAuthFlow {

        @Test
        @DisplayName("login should return tokens and user info on valid credentials")
        void loginShouldReturnTokensOnSuccess() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("admin@school.com");
            request.setPassword("password123");

            when(authService.login(any(LoginRequestDTO.class), anyString(), anyString(), any()))
                    .thenReturn(loginResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.refreshToken").exists())
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.expiresIn").value(900))
                    .andExpect(jsonPath("$.data.user.email").value("admin@school.com"))
                    .andExpect(jsonPath("$.data.user.role").value("ADMIN"))
                    .andExpect(jsonPath("$.data.user.tenantId").value("school1"));
        }

        @Test
        @DisplayName("login with 2FA enabled should return 2FA required flag without tokens")
        void loginWith2FAShouldReturn2FAFlag() throws Exception {
            LoginResponseDTO twoFactorResponse = LoginResponseDTO.builder()
                    .twoFactorRequired(true)
                    .twoFactorUserId(new UUID(0, 1))
                    .build();

            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("admin@school.com");
            request.setPassword("password123");

            when(authService.login(any(LoginRequestDTO.class), anyString(), anyString(), any()))
                    .thenReturn(twoFactorResponse);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.twoFactorRequired").value(true))
                    .andExpect(jsonPath("$.data.twoFactorUserId").value("00000000-0000-0000-0000-000000000001"))
                    .andExpect(jsonPath("$.data.accessToken").doesNotExist());
        }

        @Test
        @DisplayName("refresh should issue new tokens on valid refresh token")
        void refreshShouldIssueNewTokens() throws Exception {
            LoginResponseDTO newTokens = LoginResponseDTO.builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .tokenType("Bearer")
                    .expiresIn(900L)
                    .build();

            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("old-refresh-token");

            when(authService.refreshToken(any(RefreshTokenRequestDTO.class), anyString(), anyString()))
                    .thenReturn(newTokens);

            mockMvc.perform(post("/api/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
        }

        @Test
        @DisplayName("refresh should return 400 on expired token")
        void refreshShouldReturn400OnExpired() throws Exception {
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("expired-token");

            when(authService.refreshToken(any(RefreshTokenRequestDTO.class), anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("Refresh token is expired or revoked"));

            mockMvc.perform(post("/api/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Refresh token is expired or revoked"));
        }

        @Test
        @DisplayName("logout should succeed and return 200")
        void logoutShouldSucceed() throws Exception {
            // The JWT filter authenticates the request — simulate that here so the
            // STATELESS chain sees an authenticated user for this protected endpoint.
            doAnswer(inv -> {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("user", null, java.util.List.of()));
                inv.<jakarta.servlet.FilterChain>getArgument(2)
                        .doFilter(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("active-token");

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("login should return 400 on account lockout")
        void loginShouldReturn400OnLockout() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("locked@school.com");
            request.setPassword("password123");

            when(authService.login(any(LoginRequestDTO.class), anyString(), anyString(), any()))
                    .thenThrow(new IllegalArgumentException("Compte verrouillé. Réessayez après 25 minutes."));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Compte verrouillé. Réessayez après 25 minutes."));
        }

        @Test
        @DisplayName("login should return 400 on disabled account")
        void loginShouldReturn400OnDisabledAccount() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("disabled@school.com");
            request.setPassword("password123");

            when(authService.login(any(LoginRequestDTO.class), anyString(), anyString(), any()))
                    .thenThrow(new IllegalArgumentException("Account is disabled"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Account is disabled"));
        }
    }
}
