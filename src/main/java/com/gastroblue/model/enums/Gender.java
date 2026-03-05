package com.gastroblue.model.enums;

import com.gastroblue.model.base.ConfigurableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Gender implements ConfigurableEnum {
  MALE,
  FEMALE;
}
