package com.gastroblue.model.enums;

import com.gastroblue.model.base.DefaultConfigurableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Gender implements DefaultConfigurableEnum {
  MALE,
  FEMALE;
}
