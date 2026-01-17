package com.gastroblue.model.base;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.shared.ResolvedEnum;

public interface DefaultConfigurableEnum {
  String name();

  @SuppressWarnings("unchecked")
  default <T extends DefaultConfigurableEnum> ResolvedEnum<T> resolve(
      EnumConfigurationFacade facade, String companyId) {
    return facade.resolve((T) this, companyId);
  }

  default boolean isDefault() {
    return true;
  }
}
