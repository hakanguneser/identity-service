package com.gastroblue.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShiftType {
  MORNING,
  MID_SHIFT,
  EVENING,
  NIGHT,
  OFF,
  TRAINING;
}
