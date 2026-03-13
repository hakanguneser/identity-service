package com.gastroblue.model.enums;

import com.gastroblue.model.base.ConfigurableEnum;
import java.util.Locale;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProductRole implements ConfigurableEnum {
  GROUP_MANAGER(2),
  ZONE_MANAGER(3),
  COMPANY_MANAGER(4),
  SUPERVISOR(5),
  STAFF(6);

  private final int level;

  public boolean isZoneManager() {
    return this == ZONE_MANAGER;
  }

  public boolean isCompanyManager() {
    return this == COMPANY_MANAGER;
  }

  public boolean isSupervisor() {
    return this == SUPERVISOR;
  }

  public boolean isGroupManagerOrZoneManager() {
    return this == GROUP_MANAGER || this == ZONE_MANAGER;
  }

  public boolean isZoneManagerAndAbove() {
    return Set.of(GROUP_MANAGER, ZONE_MANAGER).contains(this);
  }

  public boolean isCompanyManagerAndAbove() {
    return Set.of(GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER).contains(this);
  }

  public boolean isSupervisorAndAbove() {
    return Set.of(GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR).contains(this);
  }

  public static ProductRole fromString(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return ProductRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
