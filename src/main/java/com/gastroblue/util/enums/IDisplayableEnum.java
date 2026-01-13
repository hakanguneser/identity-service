package com.gastroblue.util.enums;

import com.gastroblue.model.shared.EnumDisplay;
import com.gastroblue.util.EnumConfigUtil;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public interface IDisplayableEnum {

  default String getEnumCode() {
    return ((Enum<?>) this).name().toLowerCase(Locale.ENGLISH).replace("_", "-");
  }

  default String getEnumNameKebabCase() {
    String className = ((Enum<?>) this).getClass().getSimpleName();
    return className.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase(Locale.ROOT);
  }

  default String getMessageKey() {
    return "enum." + getEnumNameKebabCase() + "." + getEnumCode() + ".message";
  }

  default String getLabel() {
    return EnumConfigUtil.labelOf(getMessageKey(), LocaleContextHolder.getLocale());
  }

  default int getDisplayOrder() {
    return ((Enum<?>) this).ordinal() + 1;
  }

  default EnumDisplay toDropdownItem() {
    return new EnumDisplay(((Enum<?>) this).name(), getLabel(), getDisplayOrder());
  }
}
