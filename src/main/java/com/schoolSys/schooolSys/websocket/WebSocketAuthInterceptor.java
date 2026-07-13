package com.schoolSys.schooolSys.websocket;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.JwtTokenProvider;
import com.schoolSys.schooolSys.auth.User;
import com.schoolSys.schooolSys.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Channel interceptor that validates JWT tokens during WebSocket CONNECT
 * and sets the user principal for the session.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);

                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Principal principal = new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            List.of(() -> "ROLE_" + role)
                    );
                    accessor.setUser(principal);
                    // Store user info in session attributes for later use
                    accessor.getSessionAttributes().put("userId", userId);
                    accessor.getSessionAttributes().put("userEmail", user.getEmail());
                    accessor.getSessionAttributes().put("userRole", role);
                    log.debug("WebSocket CONNECT authenticated for user: {}", user.getEmail());
                } else {
                    log.warn("WebSocket CONNECT failed: user not found for id {}", userId);
                }
            } else {
                log.warn("WebSocket CONNECT failed: invalid or missing JWT token");
            }
        }

        return message;
    }

    /**
     * Extracts the JWT token from STOMP headers.
     * Checks both 'Authorization' header and 'token' native header.
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // Try Authorization header first
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }

        // Fall back to 'token' header
        List<String> tokenHeaders = accessor.getNativeHeader("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }

        return null;
    }
}
