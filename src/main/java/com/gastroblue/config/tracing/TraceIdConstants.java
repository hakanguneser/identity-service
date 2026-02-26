package com.gastroblue.config.tracing;

/**
 * Centralized constants for request tracing.
 *
 * <p>Using a dedicated constants class (instead of inline strings) ensures that the MDC key and the
 * HTTP header name are kept in sync across the filter, exception handler, and Logback pattern.
 */
public final class TraceIdConstants {

  /** HTTP request/response header that carries the trace ID. */
  public static final String TRACE_ID_HEADER = "X-Trace-Id";

  /** MDC key used by Logback to include the trace ID in every log line of the same thread. */
  public static final String MDC_TRACE_ID_KEY = "traceId";

  private TraceIdConstants() {
    // utility class — no instances
  }
}
