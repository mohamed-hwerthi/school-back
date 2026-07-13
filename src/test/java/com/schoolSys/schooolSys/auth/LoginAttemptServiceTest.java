package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptService Unit Tests")
class LoginAttemptServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private User user() {
        return User.builder()
                .id(new UUID(0, 1))
                .email("u@school.com")
                .passwordHash("hash")
                .firstName("U")
                .lastName("Ser")
                .role(UserRole.ENSEIGNANT)
                .isActive(true)
                .failedLoginAttempts(0)
                .twoFactorEnabled(false)
                .build();
    }

    @Test
    @DisplayName("recordFailedAttempt increments the counter")
    void recordFailedAttemptIncrements() {
        User user = user();
        when(userRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        int attempts = loginAttemptService.recordFailedAttempt(new UUID(0, 1));

        assertThat(attempts).isEqualTo(1);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    @DisplayName("recordFailedAttempt locks the account once the threshold is reached")
    void recordFailedAttemptLocksAtThreshold() {
        User user = user();
        user.setFailedLoginAttempts(LoginAttemptService.MAX_FAILED_ATTEMPTS - 1);
        when(userRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        int attempts = loginAttemptService.recordFailedAttempt(new UUID(0, 1));

        assertThat(attempts).isEqualTo(LoginAttemptService.MAX_FAILED_ATTEMPTS);
        assertThat(user.getLockedUntil()).isNotNull();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("recordFailedAttempt returns 0 for an unknown user")
    void recordFailedAttemptUnknownUser() {
        when(userRepository.findById(new UUID(0, 99))).thenReturn(Optional.empty());

        assertThat(loginAttemptService.recordFailedAttempt(new UUID(0, 99))).isZero();
    }

    @Test
    @DisplayName("resetAttempts clears the counter and the lock")
    void resetAttemptsClears() {
        User user = user();
        user.setFailedLoginAttempts(3);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        when(userRepository.findById(new UUID(0, 1))).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        loginAttemptService.resetAttempts(new UUID(0, 1));

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }
}
