package com.gastroblue.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationParams {
  FORM_DATE("#FORM_DATE#"),
  COMPANY_CODE("#COMPANY_CODE#");

  private final String valueToReplace;
}
