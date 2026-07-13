package com.schoolSys.schooolSys.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard-rail test (SEC-028): every authority or role referenced in a
 * {@code @PreAuthorize} expression must correspond to a real {@link Permission}
 * or {@link UserRole}.
 * <p>
 * A typo or a removed permission silently locks an endpoint for everyone —
 * exactly the bug fixed by SEC-019 ({@code hasAuthority('MANAGE_FINANCE')}).
 * This test fails fast if it ever happens again.
 * </p>
 */
@DisplayName("SEC-028 — Integrite de la matrice @PreAuthorize")
class PreAuthorizeIntegrityTest {

    private static final String BASE_PACKAGE = "com.schoolSys.schooolSys";

    /** Matches hasAuthority / hasAnyAuthority / hasRole / hasAnyRole calls. */
    private static final Pattern AUTH_CALL =
            Pattern.compile("has(Any)?(Authority|Role)\\s*\\(([^)]*)\\)");

    private static final Set<String> VALID_PERMISSIONS =
            Arrays.stream(Permission.values()).map(Enum::name).collect(Collectors.toSet());

    private static final Set<String> VALID_ROLES =
            Arrays.stream(UserRole.values()).map(Enum::name).collect(Collectors.toSet());

    @Test
    @DisplayName("Chaque @PreAuthorize ne reference que des Permission/UserRole reels")
    void everyPreAuthorizeReferencesKnownAuthorities() throws Exception {
        List<String> violations = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));

        for (var beanDef : scanner.findCandidateComponents(BASE_PACKAGE)) {
            Class<?> clazz;
            try {
                clazz = Class.forName(beanDef.getBeanClassName());
            } catch (Throwable unloadable) {
                continue; // not relevant to @PreAuthorize integrity
            }

            PreAuthorize classLevel = clazz.getAnnotation(PreAuthorize.class);
            if (classLevel != null) {
                checkExpression(classLevel.value(), clazz.getSimpleName(), violations);
            }
            for (Method method : clazz.getDeclaredMethods()) {
                PreAuthorize methodLevel = method.getAnnotation(PreAuthorize.class);
                if (methodLevel != null) {
                    checkExpression(methodLevel.value(),
                            clazz.getSimpleName() + "." + method.getName() + "()", violations);
                }
            }
        }

        assertThat(violations)
                .as("@PreAuthorize referencant des autorites inexistantes")
                .isEmpty();
    }

    private void checkExpression(String expression, String location, List<String> violations) {
        Matcher matcher = AUTH_CALL.matcher(expression);
        while (matcher.find()) {
            boolean isRole = "Role".equals(matcher.group(2));
            for (String rawArg : matcher.group(3).split(",")) {
                String token = stripLiteral(rawArg);
                if (token == null) {
                    continue; // SpEL variable / bean reference — not statically checkable
                }
                if (isRole) {
                    if (!VALID_ROLES.contains(token)) {
                        violations.add(location + " : role inconnu '" + token + "'");
                    }
                } else if (!isKnownAuthority(token)) {
                    violations.add(location + " : autorite inconnue '" + token + "'");
                }
            }
        }
    }

    private boolean isKnownAuthority(String token) {
        if (VALID_PERMISSIONS.contains(token)) {
            return true;
        }
        // hasAuthority('ROLE_XXX') is valid when XXX is a real role.
        return token.startsWith("ROLE_") && VALID_ROLES.contains(token.substring(5));
    }

    /** Returns the unquoted literal, or {@code null} if the argument is not a quoted string. */
    private String stripLiteral(String rawArg) {
        String arg = rawArg.trim();
        if (arg.length() >= 2
                && ((arg.startsWith("'") && arg.endsWith("'"))
                || (arg.startsWith("\"") && arg.endsWith("\"")))) {
            return arg.substring(1, arg.length() - 1).trim();
        }
        return null;
    }
}
