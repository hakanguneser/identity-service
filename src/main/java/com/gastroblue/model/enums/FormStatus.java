package com.gastroblue.model.enums;

import com.gastroblue.util.enums.IDisplayableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FormStatus implements IDisplayableEnum {
  APPROVED,
  READY_FOR_APPROVAL,
  NOT_READY_FOR_APPROVAL
}
