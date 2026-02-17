package com.gastroblue.model.base;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.shared.ResolvedEnum;

public interface ConfigurableEnum {
  String name();

  @SuppressWarnings("unchecked")
  default <T extends ConfigurableEnum> ResolvedEnum<T> resolve(
      EnumConfigurationFacade facade, String companyGroupId) {
    return facade.resolve((T) this, companyGroupId);
  }
}
