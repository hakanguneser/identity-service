package com.gastroblue.config;

import com.gastroblue.commons.helper.cache.CacheNames;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Uygulama geneli Caffeine cache yapilandirmasi.
 *
 * <p>Commons kutuphanesinin {@code CacheAutoConfig}'i
 * {@code @ConditionalOnMissingBean(CacheManager)} ile korunuyor; bu bean varken devreye girmez.
 * Commons cache'leri ({@code lookups}, {@code error-messages}) burada ayri TTL'lerle kayit edilir;
 * diger cache'ler genel Caffeine konfigurasyonunu kullanir.
 *
 * <pre>
 * Cache          | TTL    | Maks
 * lookups        | 30 dk  | 500
 * error-messages | 60 dk  | 200
 * diger          | 30 dk  | 1000
 * </pre>
 */
@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public Caffeine<Object, Object> caffeineConfig() {
    return Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(1000)
        .recordStats();
  }

  @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffeine); // diger cache'ler icin varsayilan

    // Commons: lookup cache — 30 dk, 500 giris
    cacheManager.registerCustomCache(
        CacheNames.LOOKUPS,
        Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats()
            .build());

    // Commons: error-message cache — 60 dk, 200 giris
    cacheManager.registerCustomCache(
        CacheNames.ERROR_MESSAGES,
        Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(200)
            .recordStats()
            .build());

    return cacheManager;
  }
}
