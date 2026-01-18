package com.gastroblue.model.base;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.shared.ResolvedEnum;

public interface DefaultConfigurableEnum {
  String name();

  @SuppressWarnings("unchecked")
  default <T extends DefaultConfigurableEnum> ResolvedEnum<T> resolve(
      EnumConfigurationFacade facade, String companyGroupId) {
    return facade.resolve((T) this, companyGroupId);
  }

  default boolean isDefault() {
    return true;
  }
}
