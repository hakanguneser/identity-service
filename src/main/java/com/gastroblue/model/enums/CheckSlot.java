package com.gastroblue.model.enums;

import com.gastroblue.util.enums.IConfigurableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CheckSlot implements IConfigurableEnum {
  OPENING(1),
  SHIFT_CHANGE(2),
  CLOSING(3);

  private final int displayOrder;
}
