package com.gastroblue.config;

import static com.gastroblue.model.enums.ErrorCode.EXPIRED_JWT_TOKEN;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.service.IJwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final IJwtService jwtService;

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
        SessionUser sessionUser = jwtService.extractSessionUser(jwt);
        if (jwtService.isTokenValid(sessionUser.username(), sessionUser.expiresAt())) {
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
}
