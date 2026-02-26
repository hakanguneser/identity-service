package com.gastroblue.config.tracing;

import static com.gastroblue.config.tracing.TraceIdConstants.MDC_TRACE_ID_KEY;
import static com.gastroblue.config.tracing.TraceIdConstants.TRACE_ID_HEADER;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Central request tracing filter.
 *
 * <p><b>Responsibilities:</b>
 *
 * <ol>
 *   <li>Wrap the request and response in content-caching wrappers so that the body can be read
 *       after the servlet chain has consumed it — without breaking Spring MVC's JSON parsing.
 *   <li>Resolve or generate a Trace ID and store it in MDC so that every downstream log line for
 *       this request automatically includes it.
 *   <li>Echo the Trace ID back to the caller via the {@code X-Trace-Id} response header.
 *   <li>After the chain completes, log a single structured summary line: method, URI, status, and
 *       latency.
 *   <li>Mask the {@code Authorization} header and request bodies for sensitive paths to ensure
 *       credentials never appear in log output.
 *   <li>Unconditionally clear the MDC in {@code finally} to prevent values leaking into the next
 *       request served by the same thread (thread pool reuse).
 * </ol>
 *
 * <p><b>Priority:</b> {@link Ordered#HIGHEST_PRECEDENCE} ensures this filter runs before Spring
 * Security and all other filters, so the Trace ID is available from the very first log statement
 * made by any filter in the chain.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestTracingFilter extends OncePerRequestFilter {

  private static final String MASKED_AUTH_HEADER = "Bearer [MASKED]";
  private static final int MAX_BODY_LOG_CHARS = 1_000;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 1. Wrap to enable repeatable body reads.
    // Spring 6 (Boot 4) requires an explicit buffer capacity; Integer.MAX_VALUE
    // preserves
    // the old unbounded-buffer behaviour while keeping the compiler happy.
    ContentCachingRequestWrapper wrappedRequest =
        new ContentCachingRequestWrapper(request, Integer.MAX_VALUE);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

    // 2. Resolve trace ID (client-provided or freshly generated)
    String traceId = resolveTraceId(request);

    // 3. Populate MDC so all loggers in this thread see the trace ID automatically
    MDC.put(MDC_TRACE_ID_KEY, traceId);

    // 4. Echo trace ID to the caller
    wrappedResponse.setHeader(TRACE_ID_HEADER, traceId);

    long startNanos = System.nanoTime();

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } finally {
      // 5. Log after the chain so we have the actual HTTP status code
      logRequestSummary(wrappedRequest, wrappedResponse, startNanos);

      // 6. Important: copy the cached response body back to the real output stream.
      // ContentCachingResponseWrapper buffers the body; without this step the client
      // receives an empty response.
      wrappedResponse.copyBodyToResponse();

      // 7. Always clear MDC — thread pool reuse would otherwise carry stale trace IDs
      MDC.clear();
    }
  }

  private String resolveTraceId(HttpServletRequest request) {
    String incomingTraceId = request.getHeader(TRACE_ID_HEADER);
    return (incomingTraceId != null && !incomingTraceId.isBlank())
        ? incomingTraceId
        : UUID.randomUUID().toString();
  }

  private void logRequestSummary(
      ContentCachingRequestWrapper request,
      ContentCachingResponseWrapper response,
      long startNanos) {

    long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
    String method = request.getMethod();
    String uri = request.getRequestURI();
    int status = response.getStatus();

    String authHeader = maskAuthorizationHeader(request);
    String requestBody = resolveBodyForLogging(request, uri);

    if (log.isDebugEnabled()) {
      log.debug(
          "HTTP {} {} | status={} | duration={}ms | auth={} | body={}",
          method,
          uri,
          status,
          durationMs,
          authHeader,
          requestBody);
    } else {
      log.info("HTTP {} {} | status={} | duration={}ms", method, uri, status, durationMs);
    }
  }

  /**
   * Returns a masked representation of the Authorization header.
   *
   * <p>The raw JWT token is <strong>never</strong> emitted in any log line. We only confirm whether
   * the header was present and that it uses the Bearer scheme.
   */
  private String maskAuthorizationHeader(HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || authHeader.isBlank()) {
      return "[none]";
    }
    // Always mask — even non-Bearer schemes might carry sensitive data
    return MASKED_AUTH_HEADER;
  }

  /**
   * Returns the request body as a string, or a masking placeholder for sensitive paths.
   *
   * <p>Body masking is applied for login / auth / password endpoints so that credentials cannot
   * accidentally appear in log storage (ELK, CloudWatch, etc.). The masking decision is delegated
   * to {@link SensitivePathMatcher} to keep this filter focused on I/O concerns.
   */
  private String resolveBodyForLogging(ContentCachingRequestWrapper request, String uri) {
    if (SensitivePathMatcher.isSensitive(uri)) {
      return SensitivePathMatcher.MASKED_BODY_PLACEHOLDER;
    }

    byte[] bodyBytes = request.getContentAsByteArray();
    if (bodyBytes.length == 0) {
      return "[empty]";
    }

    String body = new String(bodyBytes, StandardCharsets.UTF_8).stripLeading();
    return body.length() > MAX_BODY_LOG_CHARS
        ? body.substring(0, MAX_BODY_LOG_CHARS) + "…[TRUNCATED]"
        : body;
  }
}
