package com.gastroblue.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnumConfigUtil implements MessageSourceAware {

  private static MessageSource messageSource;

  public static boolean resolveBooleanFlag(String activePropertyKey) {
    return Boolean.parseBoolean(
        messageSource.getMessage(activePropertyKey, null, Locale.getDefault()));
  }

  @Override
  public void setMessageSource(MessageSource source) {
    messageSource = source;
  }

  public static String labelOf(String key, Locale locale) {
    return messageSource.getMessage(key, null, locale);
  }
}
