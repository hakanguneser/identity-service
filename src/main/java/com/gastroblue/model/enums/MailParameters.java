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
  FULL_NAME("fullName");

  private final String key;
}
