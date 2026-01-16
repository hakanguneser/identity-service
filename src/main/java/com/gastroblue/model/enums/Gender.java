package com.gastroblue.model.enums;

import com.gastroblue.model.base.GlobalConfigurableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Gender implements GlobalConfigurableEnum {
  MALE,
  FEMALE;
}
