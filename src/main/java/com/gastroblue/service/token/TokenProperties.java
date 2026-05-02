package com.gastroblue.service.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "gastroblue.identity.token")
public class TokenProperties {

  private String privateKey;
}
