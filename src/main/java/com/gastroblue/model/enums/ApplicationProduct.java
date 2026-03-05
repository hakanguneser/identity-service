package com.gastroblue.model.enums;

import java.util.Locale;

public enum ApplicationProduct {
  ADMIN_PANEL,
  FORMFLOW,
  THERMOMETER_TRACKER;

  public static ApplicationProduct fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return ApplicationProduct.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
