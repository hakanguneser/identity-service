package com.gastroblue.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CheckSlot {
  OPENING(1),
  SHIFT_CHANGE(2),
  CLOSING(3);

  private final int displayOrder;
}
