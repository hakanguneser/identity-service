package com.gastroblue.service;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ErrorCode;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

public interface IJwtService {
  String ANONYMOUS_USER = "anonymousUser";

  String extractUsername(String token);

  <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

  String generateToken(UserDetails userDetails);

  String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

  boolean isTokenValid(String token, UserDetails userDetails);

  static UserEntity findUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal().equals(ANONYMOUS_USER)) {
      return null;
    }
    return (UserEntity) authentication.getPrincipal();
  }

  static SessionUser findSessionUser() {
    UserEntity sessionUser = findUser();
    return UserMapper.toSessionUser(sessionUser);
  }

  static SessionUser findSessionUserOrThrow() {
    UserEntity sessionUser = findUser();
    if (sessionUser == null) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
    }
    return UserMapper.toSessionUser(sessionUser);
  }
}
