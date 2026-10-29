package com.gastroblue.service;

import com.gastroblue.commons.helper.security.service.IJwtService;
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

  /** Short-lived token accepted by every service. */
  public String generateAccessToken(String subject, Map<String, Object> claims, long expiration) {
    return generateToken(subject, claims, IJwtService.TOKEN_TYPE_ACCESS, expiration);
  }

  /** Long-lived token accepted only by the refresh endpoint. */
  public String generateRefreshToken(String subject, Map<String, Object> claims, long expiration) {
    return generateToken(subject, claims, IJwtService.TOKEN_TYPE_REFRESH, expiration);
  }

  private String generateToken(
      String subject, Map<String, Object> claims, String tokenType, long expiration) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .claims(claims)
        .claim(IJwtService.JWT_TOKEN_TYPE, tokenType)
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
