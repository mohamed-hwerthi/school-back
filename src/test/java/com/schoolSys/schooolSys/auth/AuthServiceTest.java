package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.dto.LoginRequestDTO;
import com.schoolSys.schooolSys.auth.dto.LoginResponseDTO;
import com.schoolSys.schooolSys.auth.dto.RefreshTokenRequestDTO;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TwoFactorService twoFactorService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);

        activeUser = User.builder()
                .id(new UUID(0, 1))
                .email("admin@school.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ADMIN)
                .tenantId("school1")
                .isActive(true)
                .failedLoginAttempts(0)
                .twoFactorEnabled(false)
                .build();

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("admin@school.com");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return tokens on valid credentials")
        void shouldReturnTokensOnValidCredentials() {
            when(userRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("jwt-access-token");
            when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            LoginResponseDTO result = authService.login(loginRequest, "Chrome", "127.0.0.1", null);

            assertThat(result.getAccessToken()).isEqualTo("jwt-access-token");
            assertThat(result.getRefreshToken()).isNotBlank();
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getUser().getEmail()).isEqualTo("admin@school.com");
            assertThat(result.isTwoFactorRequired()).isFalse();
        }

        @Test
        @DisplayName("should throw exception on invalid email")
        void shouldThrowOnInvalidEmail() {
            when(userRepository.findByEmail("unknown@school.com")).thenReturn(Optional.empty());

            loginRequest.setEmail("unknown@school.com");

            assertThatThrownBy(() -> authService.login(loginRequest, "Chrome", "127.0.0.1", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email or password");
        }

        @Test
        @DisplayName("should throw exception on invalid password and increment failed attempts")
        void shouldThrowOnInvalidPassword() {
            when(userRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("wrongpass", "$2a$10$hashedPassword")).thenReturn(false);
            when(loginAttemptService.recordFailedAttempt(new UUID(0, 1))).thenReturn(1);

            loginRequest.setPassword("wrongpass");

            assertThatThrownBy(() -> authService.login(loginRequest, "Chrome", "127.0.0.1", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email or password");

            verify(loginAttemptService).recordFailedAttempt(new UUID(0, 1));
        }

        @Test
        @DisplayName("should log ACCOUNT_LOCKED when attempts reach the threshold")
        void shouldLockAccountAfterMaxAttempts() {
            when(userRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("wrongpass", "$2a$10$hashedPassword")).thenReturn(false);
            when(loginAttemptService.recordFailedAttempt(new UUID(0, 1)))
                    .thenReturn(LoginAttemptService.MAX_FAILED_ATTEMPTS);

            loginRequest.setPassword("wrongpass");

            assertThatThrownBy(() -> authService.login(loginRequest, "Chrome", "127.0.0.1", null))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(auditService).logAuth(eq("ACCOUNT_LOCKED"), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should reject login when account is locked")
        void shouldRejectLoginWhenAccountLocked() {
            activeUser.setLockedUntil(LocalDateTime.now().plusMinutes(20));

            when(userRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login(loginRequest, "Chrome", "127.0.0.1", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Compte verrouillé");
        }

        @Test
        @DisplayName("should reject login when account is disabled")
        void shouldRejectLoginWhenAccountDisabled() {
            activeUser.setIsActive(false);

            when(userRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);

            assertThatThrownBy(() -> authService.login(loginRequest, "Chrome", "127.0.0.1", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account is disabled");
        }

        @Test
        @DisplayName("should reset failed attempts on successful login")
        void shouldResetFailedAttemptsOnSuccess() {
            when(userRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("jwt-token");
            when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            authService.login(loginRequest, "Chrome", "127.0.0.1", null);

            verify(loginAttemptService).resetAttempts(new UUID(0, 1));
        }

        @Test
        @DisplayName("should return 2FA required when 2FA is enabled")
        void shouldReturn2FARequiredWhen2FAEnabled() {
            activeUser.setTwoFactorEnabled(true);
            activeUser.setTotpSecret("TOTP_SECRET");

            when(userRepository.findByEmail("admin@school.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);

            LoginResponseDTO result = authService.login(loginRequest, "Chrome", "127.0.0.1", null);

            assertThat(result.isTwoFactorRequired()).isTrue();
            assertThat(result.getTwoFactorUserId()).isEqualTo(new UUID(0, 1));
            assertThat(result.getAccessToken()).isNull();
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTest {

        @Test
        @DisplayName("should issue new tokens on valid refresh token")
        void shouldIssueNewTokensOnValidRefresh() {
            RefreshToken existingToken = RefreshToken.builder()
                    .id(new UUID(0, 1))
                    .user(activeUser)
                    .token("valid-refresh-token")
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .deviceName("Chrome")
                    .ipAddress("127.0.0.1")
                    .build();

            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("valid-refresh-token");

            when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(existingToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("new-access-token");
            when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

            LoginResponseDTO result = authService.refreshToken(request, "Chrome", "127.0.0.1");

            assertThat(result.getAccessToken()).isEqualTo("new-access-token");
            assertThat(result.getRefreshToken()).isNotBlank();
            assertThat(existingToken.getRevoked()).isTrue();
        }

        @Test
        @DisplayName("should throw on invalid refresh token")
        void shouldThrowOnInvalidRefreshToken() {
            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("invalid-token");

            when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken(request, "Chrome", "127.0.0.1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("should throw on expired refresh token")
        void shouldThrowOnExpiredRefreshToken() {
            RefreshToken expiredToken = RefreshToken.builder()
                    .id(new UUID(0, 1))
                    .user(activeUser)
                    .token("expired-token")
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .revoked(false)
                    .build();

            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("expired-token");

            when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authService.refreshToken(request, "Chrome", "127.0.0.1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("expired or revoked");
        }

        @Test
        @DisplayName("should throw on revoked refresh token")
        void shouldThrowOnRevokedRefreshToken() {
            RefreshToken revokedToken = RefreshToken.builder()
                    .id(new UUID(0, 1))
                    .user(activeUser)
                    .token("revoked-token")
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .revoked(true)
                    .build();

            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("revoked-token");

            when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revokedToken));

            assertThatThrownBy(() -> authService.refreshToken(request, "Chrome", "127.0.0.1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("expired or revoked");
        }
    }

    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("should revoke refresh token on logout")
        void shouldRevokeRefreshTokenOnLogout() {
            RefreshToken token = RefreshToken.builder()
                    .id(new UUID(0, 1))
                    .user(activeUser)
                    .token("active-token")
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .build();

            RefreshTokenRequestDTO request = new RefreshTokenRequestDTO();
            request.setRefreshToken("active-token");

            when(refreshTokenRepository.findByToken("active-token")).thenReturn(Optional.of(token));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(token);

            authService.logout(request, null, null);

            assertThat(token.getRevoked()).isTrue();
            verify(refreshTokenRepository).save(token);
        }
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        @Test
        @DisplayName("should return user details")
        void shouldReturnUserDetails() {
            when(userRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(activeUser));

            var result = authService.getCurrentUser(new UUID(0, 1));

            assertThat(result.getId()).isEqualTo(new UUID(0, 1));
            assertThat(result.getEmail()).isEqualTo("admin@school.com");
            assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(new UUID(0, 999))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.getCurrentUser(new UUID(0, 999)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User not found");
        }
    }
}
