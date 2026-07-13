package com.schoolSys.schooolSys.auth;

import com.schoolSys.schooolSys.auth.dto.*;
import com.schoolSys.schooolSys.common.audit.AuditService;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.tenant.Tenant;
import com.schoolSys.schooolSys.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorService twoFactorService;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request, String deviceName,
                                  String ipAddress, String requestedTenant) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            auditService.logAuth("LOGIN_FAILED", request.getEmail(), ipAddress, "Utilisateur inconnu");
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Tenant coherence: an X-Tenant-ID header, when present, must match the
        // tenant the user actually belongs to (SUPER_ADMIN has no tenant).
        if (requestedTenant != null && !requestedTenant.isBlank()
                && !TenantContext.DEFAULT_SCHEMA.equals(requestedTenant)
                && user.getTenantId() != null
                && !requestedTenant.equals(user.getTenantId())) {
            auditService.logAuth("LOGIN_FAILED", user.getEmail(), ipAddress,
                    "Tenant demande (" + requestedTenant + ") different du tenant de l'utilisateur");
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesRemaining = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
            auditService.logAuth("LOGIN_FAILED", user.getEmail(), ipAddress,
                    "Compte verrouille (" + minutesRemaining + " min restantes)");
            throw new IllegalArgumentException(
                    "Compte verrouillé. Réessayez après " + minutesRemaining + " minutes.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Counter is persisted in its own transaction so it survives the
            // rollback caused by the exception thrown below.
            int attempts = loginAttemptService.recordFailedAttempt(user.getId());
            boolean locked = attempts >= LoginAttemptService.MAX_FAILED_ATTEMPTS;
            auditService.logAuth("LOGIN_FAILED", user.getEmail(), ipAddress,
                    "Mot de passe incorrect (tentative " + attempts
                            + "/" + LoginAttemptService.MAX_FAILED_ATTEMPTS + ")");
            if (locked) {
                auditService.logAuth("ACCOUNT_LOCKED", user.getEmail(), ipAddress,
                        "Compte verrouille " + LoginAttemptService.LOCKOUT_DURATION_MINUTES
                                + " min apres " + attempts + " echecs");
            }
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            auditService.logAuth("LOGIN_FAILED", user.getEmail(), ipAddress, "Compte desactive");
            throw new IllegalArgumentException("Account is disabled. Contact your administrator.");
        }

        // The tenant (school) must still be active.
        if (user.getTenantId() != null) {
            boolean tenantActive = tenantRepository.findBySchemaName(user.getTenantId())
                    .map(Tenant::isActive)
                    .orElse(true);
            if (!tenantActive) {
                auditService.logAuth("LOGIN_FAILED", user.getEmail(), ipAddress, "Etablissement desactive");
                throw new IllegalArgumentException(
                        "L'établissement est désactivé. Contactez l'administration.");
            }
        }

        // Reset failed attempts on successful login
        loginAttemptService.resetAttempts(user.getId());

        // If 2FA is enabled, don't return tokens yet
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            auditService.logAuth("LOGIN_2FA_REQUIRED", user.getEmail(), ipAddress,
                    "Mot de passe valide, code 2FA requis");
            return LoginResponseDTO.builder()
                    .twoFactorRequired(true)
                    .twoFactorUserId(user.getId())
                    .build();
        }

        // No 2FA — issue tokens directly
        auditService.logAuth("LOGIN_SUCCESS", user.getEmail(), ipAddress, "Connexion reussie");
        return issueTokens(user, deviceName, ipAddress);
    }

    @Transactional
    public LoginResponseDTO verifyTwoFactor(UUID userId, String code, String deviceName, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled()) || user.getTotpSecret() == null) {
            throw new IllegalArgumentException("2FA is not enabled for this user");
        }

        if (!twoFactorService.verifyCode(user.getTotpSecret(), code)) {
            auditService.logAuth("LOGIN_FAILED", user.getEmail(), ipAddress, "Code 2FA invalide");
            throw new IllegalArgumentException("Invalid 2FA code");
        }

        auditService.logAuth("LOGIN_SUCCESS", user.getEmail(), ipAddress, "Connexion reussie (2FA)");
        return issueTokens(user, deviceName, ipAddress);
    }

    @Transactional
    public Enable2FAResponseDTO enable2FA(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new IllegalArgumentException("2FA is already enabled");
        }

        String secret = twoFactorService.generateSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);

        String qrCodeUri = twoFactorService.generateQrCodeUri(secret, user.getEmail());

        return Enable2FAResponseDTO.builder()
                .secret(secret)
                .qrCodeUri(qrCodeUri)
                .build();
    }

    @Transactional
    public void confirm2FA(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getTotpSecret() == null) {
            throw new IllegalArgumentException("2FA setup has not been initiated. Call enable first.");
        }

        if (!twoFactorService.verifyCode(user.getTotpSecret(), code)) {
            throw new IllegalArgumentException("Invalid 2FA code. Please try again.");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disable2FA(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new IllegalArgumentException("2FA is not enabled");
        }

        if (!twoFactorService.verifyCode(user.getTotpSecret(), code)) {
            throw new IllegalArgumentException("Invalid 2FA code");
        }

        user.setTotpSecret(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request, String deviceName, String ipAddress) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isUsable()) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }

        // Revoke current and create new
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshTokenStr = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(toUserResponse(user))
                .build();
    }

    @Transactional
    public void logout(RefreshTokenRequestDTO request, String accessToken, String ipAddress) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                    auditService.logAuth("LOGOUT", rt.getUser().getEmail(), ipAddress, "Deconnexion");
                });
        // Blacklist the still-valid access token so logout takes effect immediately.
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            tokenBlacklistService.blacklist(accessToken, jwtTokenProvider.getExpiration(accessToken));
        }
    }

    public UserResponseDTO getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserResponse(user);
    }

    // ─── Session management ─────────────────────────────────────

    public List<SessionDTO> getActiveSessions(UUID userId, String currentToken) {
        List<RefreshToken> tokens = refreshTokenRepository.findActiveByUserId(userId);
        return tokens.stream()
                .map(rt -> SessionDTO.builder()
                        .id(rt.getId())
                        .deviceName(rt.getDeviceName())
                        .ipAddress(rt.getIpAddress())
                        .lastUsedAt(rt.getLastUsedAt())
                        .createdAt(rt.getCreatedAt())
                        .current(rt.getToken().equals(currentToken))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        RefreshToken token = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!token.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Session does not belong to the current user");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAllOtherSessions(UUID userId, String currentToken) {
        refreshTokenRepository.revokeAllOthersByUserId(userId, currentToken);
    }

    // ─── Private helpers ────────────────────────────────────────

    private LoginResponseDTO issueTokens(User user, String deviceName, String ipAddress) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(toUserResponse(user))
                .build();
    }

    private UserResponseDTO toUserResponse(User user) {
        String tenantSlug = null;
        if (user.getTenantId() != null) {
            tenantSlug = tenantRepository.findBySchemaName(user.getTenantId())
                    .map(t -> t.getSlug())
                    .orElse(null);
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .tenantId(user.getTenantId())
                .tenantSlug(tenantSlug)
                .isActive(user.getIsActive())
                .build();
    }
}
