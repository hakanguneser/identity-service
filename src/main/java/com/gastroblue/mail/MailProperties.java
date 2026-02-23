package com.gastroblue.mail;

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
