package com.gastroblue.model.properties;

import com.gastroblue.model.enums.ApplicationProduct;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {

  private Map<String, SysTokenEntry> sysTokens = new HashMap<>();

  /**
   * Builds a lookup map of {@code tokenValue → ApplicationProduct} from the configured sys-token
   * entries. Used by {@code JwtAuthenticationFilter} to identify system tokens without iterating
   * individual fields.
   */
  public Map<String, ApplicationProduct> buildTokenProductMap() {
    return sysTokens.values().stream()
        .collect(Collectors.toMap(SysTokenEntry::getToken, SysTokenEntry::getProduct));
  }

  @Getter
  @Setter
  public static class SysTokenEntry {
    private String token;
    private ApplicationProduct product;
  }
}
