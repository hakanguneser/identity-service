package com.gastroblue.service.token;

import com.gastroblue.commons.helper.jwt.model.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TokenGenerationService implements ITokenGenerationService {

  private final Key signingKey;

  public TokenGenerationService(JwtProperties jwtProperties) {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
  }

  @Override
  public String generateToken(String subject, Map<String, Object> claims, long expiration) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + expiration))
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }
}
