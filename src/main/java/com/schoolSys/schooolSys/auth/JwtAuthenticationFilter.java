package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)
                && !tokenBlacklistService.isBlacklisted(token)) {
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);
            String tenantId = jwtTokenProvider.getTenantIdFromToken(token);

            String contextTenant = TenantContext.getCurrentTenant();
            boolean contextIsDefault = TenantContext.DEFAULT_SCHEMA.equals(contextTenant);

            // Reject if X-Tenant-ID header was set and disagrees with the JWT claim
            if (tenantId != null && !contextIsDefault && !tenantId.equals(contextTenant)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "X-Tenant-ID header does not match the authenticated tenant");
                return;
            }

            // Set tenant from JWT if not already set by header
            if (tenantId != null && contextIsDefault) {
                TenantContext.setCurrentTenant(tenantId);
            }

            // Build authorities: ROLE_xxx + individual permissions from the matrix
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            try {
                UserRole userRole = UserRole.valueOf(role);
                Set<Permission> permissions = RolePermissions.getPermissions(userRole);
                for (Permission p : permissions) {
                    authorities.add(new SimpleGrantedAuthority(p.name()));
                }
            } catch (IllegalArgumentException ignored) {
                // unknown role – keep only the ROLE_ authority
            }

            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
