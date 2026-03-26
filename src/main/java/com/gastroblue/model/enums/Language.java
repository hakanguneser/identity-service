package com.gastroblue.model.enums;

import java.util.Locale;

public enum Language {
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
