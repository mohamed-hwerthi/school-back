package com.schoolSys.schooolSys.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IP-based rate limiting with stricter limits for sensitive auth endpoints.
 * <ul>
 *   <li>POST /api/auth/login — 10 req/min (brute force protection)</li>
 *   <li>POST /api/auth/forgot-password — 5 req/min (abuse protection)</li>
 *   <li>POST /api/auth/2fa/verify — 10 req/min (2FA brute force protection)</li>
 *   <li>POST /api/auth/reset-password — 5 req/min (token guessing protection)</li>
 *   <li>POST /api/inscriptions/public — 10 req/min (spam protection)</li>
 *   <li>All other endpoints — 120 req/min (general protection)</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // after TenantFilter
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int GLOBAL_LIMIT = 120;
    private static final int AUTH_LIMIT = 10;
    private static final int SENSITIVE_LIMIT = 5;

    private final Map<String, SlidingWindow> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();

        int limit = resolveLimit(method, path);
        String bucketKey = clientIp + ":" + bucketName(method, path);

        SlidingWindow window = windows.computeIfAbsent(bucketKey, k -> new SlidingWindow());

        if (window.tryConsume(limit)) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - window.getCount())));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Trop de requêtes. Réessayez dans une minute.\"}"
            );
        }
    }

    private int resolveLimit(String method, String path) {
        if (!"POST".equalsIgnoreCase(method)) return GLOBAL_LIMIT;

        if (path.equals("/api/auth/login") || path.equals("/api/auth/2fa/verify")) {
            return AUTH_LIMIT;
        }
        if (path.equals("/api/auth/forgot-password") || path.equals("/api/auth/reset-password")) {
            return SENSITIVE_LIMIT;
        }
        if (path.startsWith("/api/inscriptions/public") || path.startsWith("/api/public")) {
            return AUTH_LIMIT;
        }
        return GLOBAL_LIMIT;
    }

    private String bucketName(String method, String path) {
        if ("POST".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/auth/")) return "auth";
            if (path.startsWith("/api/inscriptions/public") || path.startsWith("/api/public")) return "public";
        }
        return "global";
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    private static class SlidingWindow {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean tryConsume(int limit) {
            long now = System.currentTimeMillis();
            if (now - windowStart > 60_000) {
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet() <= limit;
        }

        int getCount() {
            long now = System.currentTimeMillis();
            if (now - windowStart > 60_000) {
                return 0;
            }
            return count.get();
        }
    }
}
