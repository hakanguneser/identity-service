package com.gastroblue.model.enums;

import com.gastroblue.model.base.ConfigurableEnum;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApplicationRole implements ConfigurableEnum {
  ADMIN(Set.of(), 1),
  APP_CLIENT(Set.of(), 1),
  GROUP_MANAGER(Set.of(ADMIN), 2),
  ZONE_MANAGER(Set.of(ADMIN, GROUP_MANAGER), 3),
  COMPANY_MANAGER(Set.of(ADMIN, GROUP_MANAGER, ZONE_MANAGER), 4),
  SUPERVISOR(Set.of(ADMIN, GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER), 5),
  STAFF(Set.of(ADMIN, GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR), 6);

  private final Set<ApplicationRole> visibleFor;
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
