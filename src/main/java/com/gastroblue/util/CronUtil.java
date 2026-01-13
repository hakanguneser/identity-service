package com.gastroblue.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CronUtil {

  public static boolean isValidQuartzCron(String cron) {
    return CronExpression.isValidExpression(cron);
  }

  public static boolean isDayLevelCron(String cron) {
    if (cron == null) return false;
    String[] parts = cron.trim().split("\\s+");
    if (parts.length < 6 || parts.length > 7) return false;
    return "0".equals(parts[0]) && "0".equals(parts[1]) && "0".equals(parts[2]);
  }

  public static boolean matchesDay(LocalDate day, String cron) {
    try {
      return matchesDay(day, cron, ZoneId.of("Europe/Istanbul"));
    } catch (Exception e) {
      log.error("CronUtil.matchesDay", e);
      return false;
    }
  }

  public static boolean matchesDay(LocalDate day, String cron, ZoneId zoneId)
      throws ParseException {
    if (day == null) throw new IllegalArgumentException("day cannot be null");
    if (!isValidQuartzCron(cron)) {
      throw new IllegalArgumentException("Invalid Quartz cron expression: " + cron);
    }
    if (!isDayLevelCron(cron)) {
      throw new IllegalArgumentException("Cron must start with '0 0 0 ' for day-level schedules.");
    }
    CronExpression expr = new CronExpression(cron);
    expr.setTimeZone(TimeZone.getTimeZone(zoneId));
    Date candidate = Date.from(day.atStartOfDay(zoneId).toInstant());
    return expr.isSatisfiedBy(candidate);
  }
}
