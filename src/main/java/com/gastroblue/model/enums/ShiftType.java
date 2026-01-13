package com.gastroblue.model.enums;

import com.gastroblue.util.enums.IConfigurableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShiftType implements IConfigurableEnum {
  MORNING,
  MID_SHIFT,
  EVENING,
  NIGHT,
  OFF,
  TRAINING;
}
