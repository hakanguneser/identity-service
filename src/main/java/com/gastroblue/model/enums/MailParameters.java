package com.gastroblue.model.enums;

import com.gastroblue.commons.helper.mail.model.base.BaseMailParameters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailParameters implements BaseMailParameters {
  USERNAME("username"),
  TEMPORARY_PASSWORD("temporaryPassword"),
  ACTIVATE_MANAGER_NOTE("activateManagerNote"),
  MANAGER_FULL_NAME("managerFullName"),
  APPLICATION_ROLE("applicationRole"),
  DEPARTMENT("department"),
  ZONE("zone"),
  COMPANY_NAME("companyName"),
  COMPANY_GROUP_NAME("companyGroupName"),
  FULL_NAME("fullName");

  private final String key;
}
