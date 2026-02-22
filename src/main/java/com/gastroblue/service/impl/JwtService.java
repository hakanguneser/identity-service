package com.gastroblue.service.impl;

import static io.jsonwebtoken.Claims.*;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.service.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService implements IJwtService {

  @Value("${application.security.jwt.secret-key}")
  private String secretKey;

  @Value("${application.security.jwt.token-validity-in-minutes}")
  private Long tokenValidityInMinutes;

  @Value("${application.security.jwt.refresh-token-validity-in-days}")
  private Long refreshTokenValidityInDays;

  @Override
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  @Override
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  @Override
  public String generateToken(UserDetails userDetails, HashMap<String, Object> extraClaims) {
    return generateToken(extraClaims, userDetails);
  }

  @Override
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, TimeUnit.MINUTES.toMillis(tokenValidityInMinutes));
  }

  @Override
  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(
        new HashMap<>(), userDetails, TimeUnit.DAYS.toMillis(refreshTokenValidityInDays));
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  @Override
  public boolean isTokenExpired(Date tokenExpireDate) {
    return tokenExpireDate.before(new Date());
  }

  @Override
  public boolean isTokenValid(String username, Date tokenExpireDate) {
    return (username != null && !isTokenExpired(tokenExpireDate));
  }

  @Override
  public boolean isTokenExpired(String token) {
    return isTokenExpired(extractExpiration(token));
  }

  @Override
  public SessionUser extractSessionUser(String token) {
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

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
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
