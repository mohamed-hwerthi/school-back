package com.schoolSys.schooolSys.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns HTTP 401 (not Spring's default 403) when a request reaches a protected
 * endpoint without a valid JWT — typically an expired or missing access token.
 * <p>
 * The frontend's axios interceptor triggers its refresh-token flow on 401 only.
 * A 403 here would leave an expired session silently stuck: every write action
 * fails with "403" and the token is never refreshed nor the user redirected.
 * <p>
 * Genuine permission denials (valid token, insufficient role) are unaffected —
 * they still go through {@link AuditingAccessDeniedHandler} and return 403.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error("Session expirée ou authentification requise. Veuillez vous reconnecter."));
    }
}
