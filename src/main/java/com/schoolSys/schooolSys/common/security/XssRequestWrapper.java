package com.schoolSys.schooolSys.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.regex.Pattern;

/**
 * Request wrapper that sanitises parameter values and headers to prevent
 * reflected XSS attacks.
 * <p>
 * The sanitiser removes {@code <script>} blocks, event-handler attributes,
 * javascript: URIs and stray HTML tags while leaving safe content untouched.
 * </p>
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    /* ── Compiled patterns (thread-safe, compiled once) ──────── */

    private static final Pattern SCRIPT_TAG =
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SCRIPT_SRC =
            Pattern.compile("src\\s*=\\s*['\"]\\s*javascript:", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern EVAL =
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern EXPRESSION =
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern JAVASCRIPT_URI =
            Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE);

    private static final Pattern VBSCRIPT_URI =
            Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE);

    private static final Pattern ON_EVENT =
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private static final Pattern HTML_TAG =
            Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

    public XssRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /* ── Overrides ───────────────────────────────────────────── */

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return sanitize(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = sanitize(values[i]);
        }
        return sanitized;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return sanitize(value);
    }

    /* ── Sanitiser ───────────────────────────────────────────── */

    /**
     * Strips dangerous patterns from the given input string.
     * Returns {@code null} unchanged.
     */
    static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String clean = value;
        clean = SCRIPT_TAG.matcher(clean).replaceAll("");
        clean = SCRIPT_SRC.matcher(clean).replaceAll("");
        clean = EVAL.matcher(clean).replaceAll("");
        clean = EXPRESSION.matcher(clean).replaceAll("");
        clean = JAVASCRIPT_URI.matcher(clean).replaceAll("");
        clean = VBSCRIPT_URI.matcher(clean).replaceAll("");
        clean = ON_EVENT.matcher(clean).replaceAll("");
        clean = HTML_TAG.matcher(clean).replaceAll("");
        return clean;
    }
}
