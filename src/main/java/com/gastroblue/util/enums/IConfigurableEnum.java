package com.gastroblue.util.enums;

import com.gastroblue.util.EnumConfigUtil;
import java.util.Arrays;
import java.util.List;

public interface IConfigurableEnum extends IDisplayableEnum {

  default String getActiveEnum() {
    return "enum." + getEnumNameKebabCase() + "." + getEnumCode() + ".is-active";
  }

  default boolean isActive() {
    return EnumConfigUtil.resolveBooleanFlag(getActiveEnum());
  }

  static <E extends Enum<E> & IConfigurableEnum> List<E> activeEnums(Class<E> enumClass) {
    return Arrays.stream(enumClass.getEnumConstants()).filter(IConfigurableEnum::isActive).toList();
  }
}
