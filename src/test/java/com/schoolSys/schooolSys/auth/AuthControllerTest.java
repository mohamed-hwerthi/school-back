package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolSys.schooolSys.auth.JwtAuthenticationFilter;
import com.schoolSys.schooolSys.auth.JwtTokenProvider;
import com.schoolSys.schooolSys.auth.dto.LoginRequestDTO;
import com.schoolSys.schooolSys.auth.dto.LoginResponseDTO;
import com.schoolSys.schooolSys.auth.dto.RefreshTokenRequestDTO;
import com.schoolSys.schooolSys.auth.dto.UserResponseDTO;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@WithMockUser
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CurrentUserContext currentUserContext;

    private UserResponseDTO userResponse;
    private LoginResponseDTO loginResponse;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { inv.<jakarta.servlet.FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        userResponse = UserResponseDTO.builder()
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

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return 200 with tokens on valid credentials")
        void shouldReturn200WithTokens() throws Exception {
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
                    .andExpect(jsonPath("$.data.accessToken").value("jwt-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("uuid-refresh-token"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.data.user.email").value("admin@school.com"));
        }

        @Test
        @DisplayName("should return 400 on invalid credentials")
        void shouldReturn400OnInvalidCredentials() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("admin@school.com");
            request.setPassword("wrongpassword");

            when(authService.login(any(LoginRequestDTO.class), anyString(), anyString(), any()))
                    .thenThrow(new IllegalArgumentException("Invalid email or password"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));
        }

        @Test
        @DisplayName("should return 400 on missing email")
        void shouldReturn400OnMissingEmail() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setPassword("password123");
            // email is not set

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 on missing password")
        void shouldReturn400OnMissingPassword() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("admin@school.com");
            // password is not set

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 200 with 2FA required flag when 2FA is enabled")
        void shouldReturn200With2FARequired() throws Exception {
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
                    .andExpect(jsonPath("$.data.twoFactorUserId").value("00000000-0000-0000-0000-000000000001"));
        }

        @Test
        @DisplayName("should return 400 when account is locked")
        void shouldReturn400WhenAccountLocked() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO();
            request.setEmail("admin@school.com");
            request.setPassword("password123");

            when(authService.login(any(LoginRequestDTO.class), anyString(), anyString(), any()))
                    .thenThrow(new IllegalArgumentException("Compte verrouillé. Réessayez après 25 minutes."));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Compte verrouillé. Réessayez après 25 minutes."));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh-token")
    class RefreshTokenEndpoint {

        @Test
        @DisplayName("should return 200 with new tokens on valid refresh token")
        void shouldReturn200WithNewTokens() throws Exception {
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("valid-refresh-token");

            LoginResponseDTO refreshedResponse = LoginResponseDTO.builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .tokenType("Bearer")
                    .expiresIn(900L)
                    .user(userResponse)
                    .build();

            when(authService.refreshToken(any(RefreshTokenRequestDTO.class), anyString(), anyString()))
                    .thenReturn(refreshedResponse);

            mockMvc.perform(post("/api/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
        }

        @Test
        @DisplayName("should return 400 on invalid refresh token")
        void shouldReturn400OnInvalidRefreshToken() throws Exception {
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("invalid-token");

            when(authService.refreshToken(any(RefreshTokenRequestDTO.class), anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("Invalid refresh token"));

            mockMvc.perform(post("/api/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid refresh token"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutEndpoint {

        @Test
        @DisplayName("should return 200 on successful logout")
        void shouldReturn200OnLogout() throws Exception {
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("active-token");

            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
