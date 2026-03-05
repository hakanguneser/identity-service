package com.gastroblue.model.enums;

import com.gastroblue.model.base.ConfigurableEnum;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Language implements ConfigurableEnum {
  TR;

  public static Language fromString(String value) {
    if (value == null || value.isBlank()) {
      return defaultLang();
    }

    try {
      return Language.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return defaultLang();
    }
  }

  public static Language valueOf(Locale locale) {
    if (locale == null) {
      return defaultLang();
    }
    return fromString(locale.getLanguage());
  }

  public static Language defaultLang() {
    return TR;
  }
}
