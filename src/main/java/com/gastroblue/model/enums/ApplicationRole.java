package com.gastroblue.model.enums;

import com.gastroblue.model.base.ConfigurableEnum;
import java.util.Locale;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationRole implements ConfigurableEnum {
  ADMIN(1),
  APP_CLIENT(1),
  GROUP_MANAGER(2),
  ZONE_MANAGER(3),
  COMPANY_MANAGER(4),
  SUPERVISOR(5),
  STAFF(6);

  private final int level;

  public boolean isAdministrator() {
    return this == ADMIN;
  }

  public boolean isSupervisor() {
    return this == SUPERVISOR;
  }

  public boolean isZoneManager() {
    return this == ZONE_MANAGER;
  }

  public boolean isGroupManagerOrZoneManager() {
    return this == GROUP_MANAGER || this == ZONE_MANAGER;
  }

  public boolean isCompanyManager() {
    return this == COMPANY_MANAGER;
  }

  public boolean isZoneManagerAndAbove() {
    return Set.of(ADMIN, ZONE_MANAGER, GROUP_MANAGER).contains(this);
  }

  public boolean isCompanyManagerAndAbove() {
    return Set.of(ADMIN, GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER).contains(this);
  }

  public boolean isSupervisorAndAbove() {
    return Set.of(ADMIN, GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR).contains(this);
  }

  public static ApplicationRole fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return ApplicationRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
