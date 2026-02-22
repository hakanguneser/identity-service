package com.gastroblue.service;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import io.jsonwebtoken.Claims;
import java.util.*;
import java.util.function.Function;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
  String ANONYMOUS_USER = "anonymousUser";

  String extractUsername(String token);

  <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

  String generateToken(UserDetails userDetails, HashMap<String, Object> extraClaims);

  String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

  String generateRefreshToken(UserDetails userDetails);

  boolean isTokenValid(String token, String username, Date tokenExpireDate);

  boolean isTokenExpired(Date tokenExpireDate);

  boolean isTokenExpired(String token);

  static SessionUser findSessionUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || authentication.getPrincipal() == null
        || Objects.equals(authentication.getPrincipal(), ANONYMOUS_USER)) {
      return null;
    }
    return (SessionUser) authentication.getPrincipal();
  }

  static SessionUser findSessionUserOrThrow() {
    SessionUser sessionUser = findSessionUser();
    if (sessionUser == null) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }
    return sessionUser;
  }

  static Language getSessionLanguage() {
    return Optional.ofNullable(findSessionUser())
        .map(SessionUser::getLanguage)
        .orElse(Language.defaultLang());
  }
}
