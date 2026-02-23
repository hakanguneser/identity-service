package com.gastroblue.mail;

import com.gastroblue.model.enums.MailStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Centralised Micrometer metrics for the mail module.
 *
 * <p>All metrics are tagged by {@code template} and {@code status} for easy filtering in Grafana /
 * Prometheus.
 *
 * <p>Available metrics:
 *
 * <ul>
 *   <li>{@code mail.sent} – counter, tagged {@code template} + {@code status} (SUCCESS / FAILED /
 *       SKIPPED)
 *   <li>{@code mail.send.duration} – timer, tagged {@code template}
 * </ul>
 */
@Component
public class MailMetrics {

  private static final String METRIC_SENT = "mail.sent";
  private static final String METRIC_DURATION = "mail.send.duration";
  private static final String TAG_TEMPLATE = "template";
  private static final String TAG_STATUS = "status";

  private final MeterRegistry meterRegistry;

  /** Cache to avoid re-registering counters on every call. */
  private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();

  private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

  public MailMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  /** Increments the sent-mail counter for the given template + status combination. */
  public void incrementSent(MailTemplate template, MailStatus status) {
    String cacheKey = template.name() + ":" + status.name();
    counterCache
        .computeIfAbsent(
            cacheKey,
            k ->
                Counter.builder(METRIC_SENT)
                    .description("Number of emails processed")
                    .tag(TAG_TEMPLATE, template.name())
                    .tag(TAG_STATUS, status.name())
                    .register(meterRegistry))
        .increment();
  }

  /**
   * Returns the timer for tracking send duration. Use within a {@code Timer.Sample} to measure
   * elapsed time.
   */
  public Timer getSendTimer(MailTemplate template) {
    return timerCache.computeIfAbsent(
        template.name(),
        k ->
            Timer.builder(METRIC_DURATION)
                .description("Time taken to send an email via SMTP")
                .tag(TAG_TEMPLATE, template.name())
                .register(meterRegistry));
  }
}
