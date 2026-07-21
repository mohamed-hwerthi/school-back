package com.schoolSys.schooolSys.common.annee;

import com.schoolSys.schooolSys.anneescolaire.AnneeScolaire;
import com.schoolSys.schooolSys.anneescolaire.AnneeScolaireRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class AnneeFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Annee-Scolaire";

    private final AnneeScolaireRepository anneeScolaireRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String anneeLabel = request.getHeader(HEADER);

        if (anneeLabel != null && !anneeLabel.isBlank()) {
            try {
                AnneeScolaire annee = anneeScolaireRepository.findByLabel(anneeLabel.trim()).orElse(null);
                if (annee != null) {
                    AnneeContext.setCurrentLabel(annee.getLabel());
                    AnneeContext.setCurrentId(annee.getId());
                }
            } catch (Exception e) {
                log.warn("AnneeFilter: could not resolve label '{}' — table may not exist yet", anneeLabel);
            }
        }

        if (AnneeContext.getCurrentLabel() == null) {
            try {
                AnneeScolaire active = anneeScolaireRepository.findByActiveTrue().orElse(null);
                if (active != null) {
                    AnneeContext.setCurrentLabel(active.getLabel());
                    AnneeContext.setCurrentId(active.getId());
                }
            } catch (Exception e) {
                log.warn("AnneeFilter: could not resolve active annee — table may not exist yet");
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            AnneeContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth")
                || path.startsWith("/api/public")
                || path.startsWith("/api/tenants")
                || path.startsWith("/api/onboarding")
                || path.startsWith("/api/super-admin")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }
}
