package com.gastroblue.config;

import static com.gastroblue.model.enums.ErrorCode.EXPIRED_JWT_TOKEN;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.service.impl.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  private static final String BEARER = "Bearer ";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;
    if (authHeader == null || !authHeader.startsWith(BEARER)) {
      filterChain.doFilter(request, response);
      return;
    }
    try {
      jwt = authHeader.substring(BEARER.length());
      username = jwtService.extractUsername(jwt);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        SessionUser sessionUser = extractSessionUser(jwt);
        if (jwtService.isTokenValid(jwt, sessionUser.username(), sessionUser.expiresAt())) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(sessionUser, null, sessionUser.authorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContext context = SecurityContextHolder.createEmptyContext();
          context.setAuthentication(authToken);
          SecurityContextHolder.setContext(context);
        }
      }
      filterChain.doFilter(request, response);
    } catch (MalformedJwtException | ExpiredJwtException e) {
      response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
      response.getWriter().print(EXPIRED_JWT_TOKEN.name());
    }
  }

  private SessionUser extractSessionUser(String token) {
    Claims claims = jwtService.extractAllClaims(token);
    return new SessionUser(
        claims.get("aud", String.class), // applicationProduct
        claims.get("role", String.class), // applicationRole
        claims.get("cgId", String.class), // companyGroupId
        // claims.get("cIds", List.class), // companyIds (list if role ZoneManagerAndAbove)
        getCompanyIds(claims),
        claims.get("lang", String.class), // language
        claims.get("sub", String.class), // username
        claims.get("iat", Date.class), // issuedAt
        claims.get("exp", Date.class) // expiresAt
        );
  }

  @SuppressWarnings("unchecked")
  private List<String> getCompanyIds(Claims claims) {
    Object value = claims.get("cIds");

    if (value instanceof List<?> list) {
      return list.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }

    return List.of();
  }
}
