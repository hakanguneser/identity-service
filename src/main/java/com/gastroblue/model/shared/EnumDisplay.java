package com.gastroblue.model.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EnumDisplay {
  private final String key;
  private final String display;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer displayOrder;

  public static <E extends Enum<E>> EnumDisplay of(E enumValue) {
    if (enumValue == null) {
      return null;
    }
    return new EnumDisplay(enumValue.name(), getLabel(enumValue), null);
  }

  public static <E extends Enum<E>> List<EnumDisplay> of(List<E> enumValues) {
    if (enumValues == null) {
      return List.of();
    }
    return enumValues.stream().filter(Objects::nonNull).map(EnumDisplay::of).toList();
  }

  private static String getLabel(Enum<?> enumValue) {
    return "Not Implemented";
  }

  private static String getMessageKey(Enum<?> enumValue) {
    if (enumValue instanceof com.gastroblue.model.enums.ErrorCode) {
      return "error.message." + getEnumCode(enumValue);
    }
    if (enumValue instanceof com.gastroblue.model.enums.DefinitionType) {
      return "definition.type." + getEnumCode(enumValue);
    }
    return "enum." + getEnumNameKebabCase(enumValue) + "." + getEnumCode(enumValue) + ".message";
  }

  private static String getEnumNameKebabCase(Enum<?> enumValue) {
    String className = enumValue.getClass().getSimpleName();
    return className.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase(java.util.Locale.ROOT);
  }

  private static String getEnumCode(Enum<?> enumValue) {
    return enumValue.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", "-");
  }
}
