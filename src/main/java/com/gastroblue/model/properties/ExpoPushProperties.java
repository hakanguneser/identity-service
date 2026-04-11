package com.gastroblue.model.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.expo")
public class ExpoPushProperties {

  /** Expo Push API host (default matches Expo docs). */
  private String baseUrl = "https://exp.host";

  /**
   * Optional EAS access token when push security is enabled on the Expo project. See <a
   * href="https://docs.expo.dev/push-notifications/sending-notifications/">Expo push docs</a>.
   */
  private String accessToken;
}
