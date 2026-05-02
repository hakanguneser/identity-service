package com.gastroblue.service.token;

import java.util.Map;

public interface ITokenGenerationService {

  /**
   * Verilen subject ve claim'lerle imzalı JWT üretir.
   *
   * @param subject token'ın subject'i (genellikle username)
   * @param claims token'a eklenecek claim'ler — {@code IJwtService.JWT_*} sabitlerini kullan
   * @param expiration token geçerlilik süresi (millisaniye)
   * @return imzalı JWT string
   */
  String generateToken(String subject, Map<String, Object> claims, long expiration);
}
