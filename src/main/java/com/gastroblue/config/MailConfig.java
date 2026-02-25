package com.gastroblue.config;

import com.gastroblue.model.properties.MailProperties;
import java.util.Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Spring configuration for the mail module.
 *
 * <ul>
 *   <li>Declares a dedicated {@code mailTaskExecutor} thread pool.
 *   <li>Configures {@link JavaMailSender} programmatically from {@link MailProperties}.
 *   <li>Enables async processing via {@code @EnableAsync}.
 * </ul>
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

  /**
   * Dedicated executor for async mail tasks. Sized conservatively; tune via environment variables
   * if needed.
   */
  @Bean(name = "mailTaskExecutor")
  public ThreadPoolTaskExecutor mailTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("mail-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }

  /** Configures {@link JavaMailSender} from {@link MailProperties}. */
  @Bean
  public JavaMailSender javaMailSender(MailProperties props) {
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(props.getSmtp().getHost());
    sender.setPort(props.getSmtp().getPort());
    sender.setUsername(props.getSmtp().getUsername());
    sender.setPassword(props.getSmtp().getPassword());

    Properties javaMailProps = sender.getJavaMailProperties();
    javaMailProps.put("mail.transport.protocol", "smtp");
    javaMailProps.put("mail.smtp.auth", "true");
    javaMailProps.put("mail.smtp.starttls.enable", "true");
    // Disable debug – enable only in local non-production profiles if needed
    javaMailProps.put("mail.debug", "false");

    return sender;
  }
}
