package com.gastroblue.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FormStatus {
  APPROVED,
  READY_FOR_APPROVAL,
  NOT_READY_FOR_APPROVAL
}
