package com.gastroblue.model.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gastroblue.util.enums.IDisplayableEnum;
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

  public static <E extends Enum<E> & IDisplayableEnum> EnumDisplay of(E enumValue) {
    return enumValue != null ? new EnumDisplay(enumValue.name(), enumValue.getLabel(), null) : null;
  }

  public static <E extends Enum<E> & IDisplayableEnum> List<EnumDisplay> of(List<E> enumValues) {
    if (enumValues == null) {
      return List.of();
    }
    return enumValues.stream().filter(Objects::nonNull).map(EnumDisplay::of).toList();
  }
}
