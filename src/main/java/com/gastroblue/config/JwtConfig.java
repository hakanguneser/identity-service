package com.gastroblue.config;

import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.properties.JwtProperties;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

  /**
   * Exposes the sys-token → product lookup map as a bean so it can be injected wherever
   * token-to-product resolution is needed (e.g. {@code JwtAuthenticationFilter}).
   */
  @Bean
  public Map<String, ApplicationProduct> sysTokenProductMap(JwtProperties jwtProperties) {
    return jwtProperties.buildTokenProductMap();
  }
}
