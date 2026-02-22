package com.gastroblue.model.enums;

import com.gastroblue.model.base.ConfigurableEnum;
import lombok.Getter;

@Getter
public enum Zone implements ConfigurableEnum {
  ZONE_1,
  ZONE_2,
  ZONE_3,
  ZONE_4,
  ZONE_5,
  ZONE_6,
  ZONE_7,
  ZONE_8,
  ZONE_9,
  ZONE_10;

  public static Zone fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Zone.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
