package com.gastroblue.service;

import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenGenerationService {

  private final PrivateKey privateKey;

  public TokenGenerationService(
      @Value("${gastroblue.identity.token.private-key}") String privateKeyBase64) {
    this.privateKey = loadPrivateKey(privateKeyBase64);
  }

  public String generateToken(String subject, Map<String, Object> claims, long expiration) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date(now))
        .expiration(new Date(now + expiration))
        .signWith(privateKey)
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
