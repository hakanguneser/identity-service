package com.gastroblue.config.tracing;

import java.util.List;

/**
 * Determines whether a given request URI is considered sensitive.
 *
 * <p>For sensitive paths the request body is <strong>never</strong> logged; instead a placeholder
 * is emitted. Masking is applied at this layer (the filter) rather than inside service logic,
 * because the filter is the last boundary that still has access to the raw HTTP stream before it is
 * consumed by Jackson/Spring.
 *
 * <p>Why a separate class: single-responsibility, easy to unit-test, and straightforward to extend
 * without touching the filter itself.
 */
public final class SensitivePathMatcher {

  /**
   * URI fragments that indicate the request body may contain credentials or PII. Matching is
   * case-insensitive and substring-based so that versioned paths (e.g. {@code /api/v1/auth/login})
   * are covered automatically.
   */
  private static final List<String> SENSITIVE_SEGMENTS =
      List.of("/auth/login", "/auth/refresh", "/auth/register", "/password", "/credentials");

  public static final String MASKED_BODY_PLACEHOLDER = "[BODY MASKED - SENSITIVE PATH]";

  private SensitivePathMatcher() {}

  /**
   * Returns {@code true} when the given URI contains a segment that may carry credentials or other
   * sensitive data that must never appear in log output.
   */
  public static boolean isSensitive(String uri) {
    if (uri == null || uri.isBlank()) {
      return false;
    }
    String lowerUri = uri.toLowerCase();
    return SENSITIVE_SEGMENTS.stream().anyMatch(lowerUri::contains);
  }
}
