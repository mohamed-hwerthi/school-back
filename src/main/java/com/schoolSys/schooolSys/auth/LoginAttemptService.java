package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persists login-attempt counters in their own transaction.
 * <p>
 * {@code AuthService.login()} throws on a bad password, which rolls back its
 * transaction — so the failed-attempt counter must be written in a separate
 * {@code REQUIRES_NEW} transaction, otherwise the account lockout never trips.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCKOUT_DURATION_MINUTES = 30;

    private final UserRepository userRepository;

    /**
     * Records one failed login attempt and locks the account once the
     * threshold is reached. Runs in its own transaction so the counter
     * survives the rollback of the caller's failed login.
     *
     * @param userId the user who failed to authenticate
     * @return the new failed-attempt count (0 if the user no longer exists)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return 0;
        }
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        }
        userRepository.save(user);
        return attempts;
    }

    /**
     * Clears the failed-attempt counter and any lock after a successful login.
     *
     * @param userId the user who authenticated successfully
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetAttempts(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }
        });
    }
}
