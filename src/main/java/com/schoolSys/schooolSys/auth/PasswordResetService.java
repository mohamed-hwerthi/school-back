package com.schoolSys.schooolSys.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int TOKEN_EXPIRY_HOURS = 1;

    @Transactional
    public String requestPasswordReset(String email) {
        // Always return success message (don't reveal if email exists)
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                    .build();

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(email, token);
        });

        return "Si un compte existe avec cet email, un lien de réinitialisation a été envoyé.";
    }

    @Transactional
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de réinitialisation invalide ou expiré."));

        if (!resetToken.isUsable()) {
            throw new IllegalArgumentException("Token de réinitialisation invalide ou expiré.");
        }

        User user = resetToken.getUser();

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Revoke all existing refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Password reset successful for user: {}", user.getEmail());

        return "Mot de passe réinitialisé avec succès.";
    }
}
