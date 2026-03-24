package com.gastroblue.model.base;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.shared.ResolvedEnum;

public interface ConfigurableEnum {
  String name();

  /**
   * Human-readable default label used when no {@link
   * com.gastroblue.model.entity.EnumValueConfigurationEntity} record exists yet. Returns {@link
   * #name()} by default; individual enums may override for friendlier initial labels.
   */
  default String getDefaultLabel() {
    return name();
  }

  default <T extends ConfigurableEnum> ResolvedEnum resolve(
      EnumConfigurationFacade facade, String companyGroupId) {
    return facade.resolve((T) this, companyGroupId);
  }
}
