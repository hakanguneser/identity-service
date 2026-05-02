package com.gastroblue.service.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    name = "gastroblue.commons.security.enabled",
    havingValue = "true",
    matchIfMissing = false)
@EnableConfigurationProperties(TokenProperties.class)
public class TokenGenerationService implements ITokenGenerationService {

  private final PrivateKey privateKey;

  public TokenGenerationService(TokenProperties tokenProperties) {
    this.privateKey = loadPrivateKey(tokenProperties.getPrivateKey());
  }

  @Override
  public String generateToken(String subject, Map<String, Object> claims, long expiration) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + expiration))
        .signWith(privateKey, SignatureAlgorithm.ES256)
        .compact();
  }

  private static PrivateKey loadPrivateKey(String base64Key) {
    try {
      byte[] keyBytes = Base64.getDecoder().decode(base64Key);
      // EC P-256 (PKCS#8 formatı)
      return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    } catch (Exception e) {
      throw new IllegalStateException("EC private key yüklenemedi: " + e.getMessage(), e);
    }
  }
}
