package com.schoolSys.schooolSys.common.exception;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Global exception handler that translates exceptions into
 * consistent {@link ApiResponse} error payloads.
 * <p>
 * Each handler returns an appropriate HTTP status code and a
 * human-readable error message without leaking internal details.
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles requests for non-existent resources.
     *
     * @param ex the exception
     * @return 404 response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles invalid arguments (e.g. bad schema name).
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles database constraint violations.
     * <ul>
     *   <li>Unique constraint / foreign key → 409 Conflict (collision avec l'état serveur)</li>
     *   <li>NOT NULL constraint → 400 Bad Request (champ obligatoire manquant côté client)</li>
     * </ul>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DataIntegrityViolationException ex) {
        String causeMessage = ex.getMostSpecificCause().getMessage();
        log.warn("Data integrity violation: {}", causeMessage);

        HttpStatus status = isNotNullViolation(causeMessage)
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(ApiResponse.error(translateConstraintViolation(causeMessage)));
    }

    private boolean isNotNullViolation(String causeMessage) {
        return causeMessage != null
                && (causeMessage.contains("not-null") || causeMessage.contains("null value"));
    }

    private static final Pattern UNIQUE_CONSTRAINT_PATTERN =
            Pattern.compile("unique constraint \"([^\"]+)\"");
    private static final Pattern DETAIL_KEY_PATTERN =
            Pattern.compile("Key \\(([^)]+)\\)=\\(([^)]*)\\)");
    private static final Pattern FK_CONSTRAINT_PATTERN =
            Pattern.compile("foreign key constraint \"([^\"]+)\"");

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("email", "email"),
            Map.entry("matricule", "matricule"),
            Map.entry("registration_number", "numéro d'inscription"),
            Map.entry("username", "nom d'utilisateur"),
            Map.entry("phone", "téléphone"),
            Map.entry("cin", "CIN"),
            Map.entry("name", "nom"),
            Map.entry("slug", "identifiant"),
            Map.entry("code", "code")
    );

    private String translateConstraintViolation(String causeMessage) {
        if (causeMessage == null) {
            return "Violation de contrainte d'intégrité";
        }

        Matcher detail = DETAIL_KEY_PATTERN.matcher(causeMessage);
        String column = null;
        String value = null;
        if (detail.find()) {
            column = detail.group(1).trim();
            value = detail.group(2).trim();
        }

        if (causeMessage.contains("duplicate key") || causeMessage.contains("unique constraint")) {
            String field = column != null ? friendlyField(column) : null;
            if (field == null) {
                Matcher uq = UNIQUE_CONSTRAINT_PATTERN.matcher(causeMessage);
                if (uq.find()) field = friendlyField(extractColumnFromConstraint(uq.group(1)));
            }
            if (field != null) {
                return value != null && !value.isEmpty()
                        ? "Le champ « " + field + " » avec la valeur « " + value + " » existe déjà"
                        : "Le champ « " + field + " » doit être unique — cette valeur existe déjà";
            }
            return "Cette valeur existe déjà (contrainte d'unicité)";
        }

        if (causeMessage.contains("foreign key") || causeMessage.contains("violates foreign key")) {
            Matcher fk = FK_CONSTRAINT_PATTERN.matcher(causeMessage);
            String ref = fk.find() ? fk.group(1) : null;
            return ref != null
                    ? "Référence invalide ou utilisée ailleurs (" + ref + ")"
                    : "Cette donnée est référencée par un autre enregistrement et ne peut pas être modifiée/supprimée";
        }

        if (causeMessage.contains("not-null") || causeMessage.contains("null value")) {
            return column != null
                    ? "Le champ « " + friendlyField(column) + " » est obligatoire"
                    : "Un champ obligatoire est manquant";
        }

        return "Violation de contrainte d'intégrité";
    }

    private String extractColumnFromConstraint(String constraintName) {
        if (constraintName == null) return null;
        String stripped = constraintName;
        if (stripped.endsWith("_key")) stripped = stripped.substring(0, stripped.length() - 4);
        int firstUnderscore = stripped.indexOf('_');
        return firstUnderscore >= 0 ? stripped.substring(firstUnderscore + 1) : stripped;
    }

    private String friendlyField(String column) {
        if (column == null) return null;
        String key = column.toLowerCase().trim();
        return FIELD_LABELS.getOrDefault(key, key.replace('_', ' '));
    }

    /**
     * Handles malformed JSON or unreadable request bodies.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Malformed request body"));
    }

    /**
     * Handles unsupported HTTP methods.
     *
     * @param ex the exception
     * @return 405 response
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("HTTP method '" + ex.getMethod() + "' is not supported for this endpoint"));
    }

    /**
     * Handles requests to non-existent endpoints.
     *
     * @param ex the exception
     * @return 404 response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Endpoint not found"));
    }

    /**
     * Catch-all handler for unexpected exceptions.
     *
     * @param ex the exception
     * @return 500 response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed: " + errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied"));
    }

    /**
     * Handles authentication failures (bad credentials, expired tokens, etc.).
     *
     * @param ex the exception
     * @return 401 response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed"));
    }

    /**
     * Handles bad credentials specifically for clearer messaging.
     *
     * @param ex the exception
     * @return 401 response
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials"));
    }

    /**
     * Handles missing required request parameters.
     *
     * @param ex the exception
     * @return 400 response
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Missing required parameter: " + ex.getParameterName()));
    }

    /**
     * Handles path variable / query param type mismatch (ex: /students/abc où id attend un Long).
     * Sans ce handler la NumberFormatException remonte au handler générique → 500.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valide";
        String msg = "Paramètre '" + ex.getName() + "' invalide : type " + requiredType + " attendu";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg));
    }

    /**
     * Catch-all handler for unexpected exceptions.
     * Never exposes stack traces or internal details to clients.
     *
     * @param ex the exception
     * @return 500 response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
