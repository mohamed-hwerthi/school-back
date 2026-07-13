package com.schoolSys.schooolSys.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class CorsFilter extends OncePerRequestFilter {

    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:5000",
            "http://localhost:5001",
            "http://localhost:5173",
            "http://localhost:3000",
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");

        if (origin != null) {
            boolean allowed = false;
            for (String o : ALLOWED_ORIGINS) {
                if (o.equals(origin)) {
                    allowed = true;
                    break;
                }
            }
            if (allowed) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Access-Control-Expose-Headers", "Authorization, X-Tenant-ID");
                response.setHeader("Access-Control-Max-Age", "3600");

                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
