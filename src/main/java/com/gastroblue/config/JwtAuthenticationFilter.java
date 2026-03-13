package com.gastroblue.config;

import static com.gastroblue.model.enums.ErrorCode.EXPIRED_JWT_TOKEN;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.service.IJwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

  @Value("${application.security.jwt.sys-tokens.tt}")
  private String ttToken;

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

        SessionUser sessionUser;
        if (jwtToken.equals(ttToken)) {
          sessionUser =
              new SessionUser(
                  ApplicationProduct.TRACKER.name(),
                  ApplicationRole.APP_CLIENT.name(),
                  List.of(),
                  null,
                  List.of(),
                  "TR",
                  "TT",
                  new Date(),
                  new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365));
        } else {
          sessionUser = jwtService.validateAndExtractToken(jwtToken);
        }

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
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().print(EXPIRED_JWT_TOKEN.name());
      return;

    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token", e);
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().print("INVALID_JWT_TOKEN");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
