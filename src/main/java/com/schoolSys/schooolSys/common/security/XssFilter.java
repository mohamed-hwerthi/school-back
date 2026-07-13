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

/**
 * Servlet filter that wraps every incoming request in an {@link XssRequestWrapper}
 * to strip potentially dangerous HTML / JavaScript from query parameters and headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        filterChain.doFilter(new XssRequestWrapper(request), response);
    }
}
