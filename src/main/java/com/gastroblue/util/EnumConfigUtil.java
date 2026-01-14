package com.gastroblue.util;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnumConfigUtil {

  private final MessageSource messageSource;

  public boolean resolveBooleanFlag(String activePropertyKey) {
    return Boolean.parseBoolean(labelOf(activePropertyKey, Locale.getDefault()));
  }

  public String labelOf(String key, Locale locale) {
    try {
      return messageSource.getMessage(key, null, locale);
    } catch (NoSuchMessageException nse) {
      log.error("Message Not Found for " + key + locale, nse);
    }
    return "BJK";
  }
}
