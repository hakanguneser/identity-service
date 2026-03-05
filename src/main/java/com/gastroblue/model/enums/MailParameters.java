package com.gastroblue.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailParameters {
  USERNAME("username"),
  TEMPORARY_PASSWORD("temporaryPassword"),
  ACTIVATE_MANAGER_NOTE("activateManagerNote"),
  MANAGER_FULL_NAME("managerFullname"),
  APPLICATION_ROLE("applicationRole"),
  DEPARTMENT("department"),
  ZONE("zone"),
  COMPANY_NAME("companyName"),
  COMPANY_GROUP_NAME("companyGroupName"),
  FULL_NAME("fullName");

  private final String key;
}
