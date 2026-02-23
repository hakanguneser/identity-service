package com.gastroblue.service.impl;

import static io.jsonwebtoken.Claims.*;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.service.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService implements IJwtService {

  @Value("${application.security.jwt.secret-key}")
  private String secretKey;

  @Override
  public String generateToken(
      String username, HashMap<String, Object> extraClaims, long expiration) {
    return buildToken(username, extraClaims, expiration);
  }

  public SessionUser validateAndExtractToken(String token) throws AccessDeniedException {
    SessionUser sessionUser = extractSessionUser(token);
    validateToken(sessionUser);
    return sessionUser;
  }

  private void validateToken(SessionUser sessionUser) {
    isTokenExpired(sessionUser.expiresAt());
  }

  private void isTokenExpired(Date tokenExpireDate) {
    if (tokenExpireDate.before(new Date())) {
      throw new AccessDeniedException(ErrorCode.EXPIRED_JWT_TOKEN);
    }
  }

  private String buildToken(String username, Map<String, Object> extraClaims, long expiration) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private SessionUser extractSessionUser(String token) {
    Claims claims = extractAllClaims(token);
    return new SessionUser(
        claims.get(JWT_APPLICATION_PRODUCT, String.class),
        claims.get(JWT_ROLE, String.class),
        claims.get(JWT_COMPANY_GROUP_ID, String.class),
        getCompanyIds(claims),
        claims.get(JWT_LANGUAGE, String.class),
        claims.get(SUBJECT, String.class),
        claims.get(ISSUED_AT, Date.class),
        claims.get(EXPIRATION, Date.class));
  }

  @SuppressWarnings("unchecked")
  private List<String> getCompanyIds(Claims claims) {
    Object value = claims.get(JWT_COMPANY_IDS);
    if (value instanceof List<?> list) {
      return list.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }
    return List.of();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
