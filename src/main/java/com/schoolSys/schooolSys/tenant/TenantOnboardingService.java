package com.schoolSys.schooolSys.tenant;

import com.schoolSys.schooolSys.auth.User;
import com.schoolSys.schooolSys.auth.UserRepository;
import com.schoolSys.schooolSys.auth.UserRole;
import com.schoolSys.schooolSys.common.multitenancy.TenantFlywayConfig;
import com.schoolSys.schooolSys.tenant.dto.TenantOnboardingRequest;
import com.schoolSys.schooolSys.tenant.dto.TenantResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantOnboardingService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final TenantFlywayConfig tenantFlywayConfig;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private static final Map<String, PlanDefaults> PLAN_DEFAULTS = Map.of(
            "FREE", new PlanDefaults(50, 10, 500, BigDecimal.ZERO),
            "STANDARD", new PlanDefaults(200, 30, 2000, BigDecimal.valueOf(49.99)),
            "PREMIUM", new PlanDefaults(500, 100, 10000, BigDecimal.valueOf(149.99)),
            "ENTERPRISE", new PlanDefaults(Integer.MAX_VALUE, Integer.MAX_VALUE, 50000, BigDecimal.valueOf(499.99))
    );

    @Transactional
    public TenantResponseDTO onboard(TenantOnboardingRequest request) {
        String baseSlug = slugify(request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug()
                : request.getSchoolName());

        // Generate a guaranteed-unique schema name — auto-suffixed if the base
        // is already taken, so two schools with the same name never collide.
        String schemaName = generateUniqueSchemaName(baseSlug);
        String slug = schemaName.replace("_", "-");

        if (request.getAdminEmail() == null || request.getAdminEmail().isBlank()
                || request.getAdminPassword() == null || request.getAdminPassword().isBlank()) {
            throw new IllegalArgumentException("Admin email and password are required");
        }

        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        // Determine plan
        String plan = request.getPlan() != null ? request.getPlan().toUpperCase() : "FREE";
        PlanDefaults defaults = PLAN_DEFAULTS.getOrDefault(plan, PLAN_DEFAULTS.get("FREE"));

        // Create and initialize tenant schema
        tenantFlywayConfig.migrateTenantSchema(schemaName);

        // Sync the real school name into the new schema's school_settings.
        // Migration V23 seeds that table with a hardcoded demo name
        // ("École Primaire Ibn Khaldoun"); without this update the school
        // dashboard would display the demo name instead of the real one.
        // schemaName is slug-validated (^[a-z][a-z0-9_]{2,62}$) — safe to inline.
        jdbcTemplate.update(
                "UPDATE \"" + schemaName + "\".school_settings SET "
                        + "school_name = ?, school_name_ar = ?, "
                        + "delegation_regionale = ?, delegation_regionale_ar = ?, "
                        + "adresse = NULL, telephone = NULL, "
                        + "directeur_name = NULL, directeur_name_ar = NULL, "
                        + "updated_at = NOW()",
                request.getSchoolName(),
                request.getSchoolNameAr(),
                request.getDelegationRegionale(),
                request.getDelegationRegionaleAr());

        Tenant tenant = Tenant.builder()
                .name(request.getSchoolName())
                .schemaName(schemaName)
                .slug(slug)
                .contactEmail(request.getAdminEmail())
                .active(true)
                .plan(plan)
                .maxStudents(defaults.maxStudents)
                .maxTeachers(defaults.maxTeachers)
                .maxStorageMb(defaults.maxStorageMb)
                .monthlyRate(defaults.monthlyRate)
                .billingEmail(request.getAdminEmail())
                .billingCycle("MONTHLY")
                .trialEndsAt("FREE".equals(plan) ? null : LocalDate.now().plusDays(30))
                .onboardingCompleted(true)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        log.info("Onboarded new tenant: {} (schema: {}, plan: {})", saved.getName(), schemaName, plan);

        User admin = User.builder()
                .email(request.getAdminEmail())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .firstName(request.getAdminFirstName() != null ? request.getAdminFirstName() : "Admin")
                .lastName(request.getAdminLastName() != null ? request.getAdminLastName() : request.getSchoolName())
                .role(UserRole.ADMIN)
                .tenantId(schemaName)
                .isActive(true)
                .build();
        userRepository.save(admin);
        log.info("Created admin user {} for tenant {}", admin.getEmail(), schemaName);

        return tenantMapper.toResponseDTO(saved);
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Builds a PostgreSQL-safe, globally unique schema name from a slug.
     * <p>If the derived name is already used by another tenant, a numeric
     * suffix ({@code _2}, {@code _3}, …) is appended until a free name is
     * found. The result always matches {@code ^[a-z][a-z0-9_]{2,62}$}.
     *
     * @param baseSlug the slugified school name (may be empty for non-Latin names)
     * @return a unique schema name not yet present in the {@code tenants} table
     */
    private String generateUniqueSchemaName(String baseSlug) {
        String base = baseSlug.replace("-", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        // Schema names must be non-empty and start with a letter
        if (base.isBlank() || !Character.isLetter(base.charAt(0))) {
            base = ("ecole_" + base).replaceAll("_+", "_").replaceAll("_$", "");
        }
        // PostgreSQL identifiers cap at 63 chars — keep room for a "_NN" suffix
        if (base.length() > 58) {
            base = base.substring(0, 58).replaceAll("_$", "");
        }
        // Pad very short names to satisfy the minimum length (3 chars)
        while (base.length() < 3) {
            base = base + "x";
        }

        String candidate = base;
        int suffix = 2;
        while (tenantRepository.existsBySchemaName(candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    private record PlanDefaults(int maxStudents, int maxTeachers, int maxStorageMb, BigDecimal monthlyRate) {}
}
