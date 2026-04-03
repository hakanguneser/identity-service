package com.gastroblue.config;

import static com.gastroblue.model.enums.ErrorCode.EXPIRED_JWT_TOKEN;
import static com.gastroblue.model.enums.ErrorCode.INVALID_JWT_TOKEN;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gastroblue.config.tracing.TraceIdConstants;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.exception.ApplicationError;
import com.gastroblue.service.IJwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER = "Bearer ";

  private final IJwtService jwtService;
  private final Map<String, ApplicationProduct> sysTokenProductMap;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(BEARER)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String jwtToken = authHeader.substring(BEARER.length());

      if (SecurityContextHolder.getContext().getAuthentication() == null) {

        SessionUser sessionUser =
            sysTokenProductMap.containsKey(jwtToken)
                ? buildSysTokenSession(sysTokenProductMap.get(jwtToken))
                : jwtService.validateAndExtractToken(jwtToken);

        // Log parsed, non-sensitive claims only — the raw token is never emitted
        log.debug(
            "JWT validated | subject={} | roles={}",
            sessionUser.username(),
            sessionUser.authorities());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(sessionUser, null, sessionUser.authorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
      }

    } catch (ExpiredJwtException e) {
      log.debug("Expired JWT token", e);
      writeJsonError(response, HttpStatus.UNAUTHORIZED, EXPIRED_JWT_TOKEN);
      return;

    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token", e);
      writeJsonError(response, HttpStatus.UNAUTHORIZED, INVALID_JWT_TOKEN);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private SessionUser buildSysTokenSession(ApplicationProduct product) {
    ApplicationRole role =
        product == ApplicationProduct.CHECK ? ApplicationRole.ADMIN : ApplicationRole.APP_CLIENT;
    Date now = new Date();
    Date expiresAt = new Date(now.getTime() + 1000L * 60 * 60 * 24 * 365);
    return new SessionUser(
        null,
        product.name(),
        role.name(),
        List.of(),
        null,
        List.of(),
        "TR",
        product.name(),
        now,
        expiresAt);
  }

  private void writeJsonError(HttpServletResponse response, HttpStatus status, ErrorCode errorCode)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ApplicationError error =
        ApplicationError.builder()
            .errorCode(errorCode)
            .httpStatus(status)
            .timeStamp(LocalDateTime.now())
            .traceId(MDC.get(TraceIdConstants.MDC_TRACE_ID_KEY))
            .build();
    objectMapper.writeValue(response.getWriter(), error);
  }
}
