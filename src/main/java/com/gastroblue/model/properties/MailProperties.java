package com.gastroblue.model.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

  /**
   * Whether to actually dispatch emails. When {@code false}, sends are skipped but still logged.
   */
  private boolean enabled = true;

  /** The sender address that appears in the From header. */
  private String from;

  /**
   * When set, all outgoing emails are redirected to this address instead of the real recipients.
   * Intended for non-production environments only. Configure via {@code
   * mail.admin-redirect-address}.
   */
  private List<String> adminRedirectAddress;

  private Smtp smtp = new Smtp();

  @Getter
  @Setter
  public static class Smtp {
    private String host = "smtp.gmail.com";
    private int port = 587;
    private String username;
    private String password;
  }
}
