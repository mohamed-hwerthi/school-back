package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.dto.*;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.security.CurrentUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final CurrentUserContext currentUserContext;

    // ─── Login / Token ──────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {
        String deviceName = extractDeviceName(httpRequest);
        String ipAddress = extractIpAddress(httpRequest);
        String requestedTenant = httpRequest.getHeader("X-Tenant-ID");
        LoginResponseDTO response = authService.login(request, deviceName, ipAddress, requestedTenant);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request,
            HttpServletRequest httpRequest) {
        String deviceName = extractDeviceName(httpRequest);
        String ipAddress = extractIpAddress(httpRequest);
        LoginResponseDTO response = authService.refreshToken(request, deviceName, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDTO request,
            HttpServletRequest httpRequest) {
        authService.logout(request, extractBearerToken(httpRequest), extractIpAddress(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<com.schoolSys.schooolSys.auth.dto.MeResponseDTO>> getCurrentUser(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserResponseDTO user = authService.getCurrentUser(userId);

        java.util.List<String> perms = com.schoolSys.schooolSys.auth.RolePermissions
                .getPermissions(user.getRole()).stream()
                .map(Enum::name).toList();

        java.util.Set<UUID> scopedClasses = currentUserContext.hasRole(
                com.schoolSys.schooolSys.auth.UserRole.ENSEIGNANT)
                ? currentUserContext.getScopedClasseIdsForTeacher()
                : java.util.Set.of();

        java.util.Set<UUID> scopedStudents = currentUserContext.hasRole(
                com.schoolSys.schooolSys.auth.UserRole.PARENT)
                ? currentUserContext.getScopedStudentIdsForParent()
                : java.util.Set.of();

        com.schoolSys.schooolSys.auth.dto.MeResponseDTO me = com.schoolSys.schooolSys.auth.dto.MeResponseDTO.builder()
                .user(user)
                .permissions(perms)
                .scopedClasseIds(scopedClasses)
                .scopedStudentIds(scopedStudents)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(me));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        String message = passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        String message = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    // ─── 2FA ────────────────────────────────────────────────────

    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse<Enable2FAResponseDTO>> enable2FA(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        Enable2FAResponseDTO response = authService.enable2FA(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm2FA(
            Authentication authentication,
            @Valid @RequestBody Verify2FADTO request) {
        UUID userId = (UUID) authentication.getPrincipal();
        authService.confirm2FA(userId, request.getCode());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> verify2FA(
            @Valid @RequestBody Verify2FALoginDTO request,
            HttpServletRequest httpRequest) {
        String deviceName = extractDeviceName(httpRequest);
        String ipAddress = extractIpAddress(httpRequest);
        LoginResponseDTO response = authService.verifyTwoFactor(
                request.getUserId(), request.getCode(), deviceName, ipAddress);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<Void>> disable2FA(
            Authentication authentication,
            @Valid @RequestBody Verify2FADTO request) {
        UUID userId = (UUID) authentication.getPrincipal();
        authService.disable2FA(userId, request.getCode());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ─── Session management ─────────────────────────────────────

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SessionDTO>>> getActiveSessions(
            Authentication authentication,
            @RequestHeader(value = "X-Current-Refresh-Token", required = false) String currentToken) {
        UUID userId = (UUID) authentication.getPrincipal();
        List<SessionDTO> sessions = authService.getActiveSessions(userId, currentToken);
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            Authentication authentication,
            @PathVariable UUID sessionId) {
        UUID userId = (UUID) authentication.getPrincipal();
        authService.revokeSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<ApiResponse<Void>> revokeAllOtherSessions(
            Authentication authentication,
            @RequestHeader(value = "X-Current-Refresh-Token", required = false) String currentToken) {
        UUID userId = (UUID) authentication.getPrincipal();
        String token = currentToken != null ? currentToken : "";
        authService.revokeAllOtherSessions(userId, token);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ─── Helpers ────────────────────────────────────────────────

    private String extractDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        if (userAgent.length() > 200) {
            return userAgent.substring(0, 200);
        }
        return userAgent;
    }

    /** Extracts the raw JWT from the {@code Authorization: Bearer ...} header. */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
