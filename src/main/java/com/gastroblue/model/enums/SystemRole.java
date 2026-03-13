package com.gastroblue.model.enums;

import java.util.Locale;

public enum SystemRole {
  ADMIN,
  APP_CLIENT,
  USER;

  public boolean isAdmin() {
    return this == ADMIN;
  }

  public static SystemRole fromString(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return SystemRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
