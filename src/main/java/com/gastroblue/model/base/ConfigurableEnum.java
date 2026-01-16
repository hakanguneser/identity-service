package com.gastroblue.model.base;

import com.gastroblue.model.enums.Language;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.EnumConfigurationService;

public interface ConfigurableEnum {
  String name();

  @SuppressWarnings("unchecked")
  default <T extends ConfigurableEnum> ResolvedEnum<T> resolve(
      EnumConfigurationService service, String companyGroupId) {
    return service.resolve((T) this, companyGroupId, Language.defaultLang().name());
  }
}
