package com.gastroblue.model.enums;

public enum DefinitionType {
  COMPANY,
  COMPANY_GROUP,
  PERSONNEL_GROUP,
  PERSONNEL_GROUP_TASK_ASSIGNMENT,
  TASK,
  TASK_DEFINITION,
  MEASUREMENT,
  MEASUREMENT_ASSET_GROUP,
  MEASUREMENT_ASSET_GROUP_ITEM,
  WITNESS_SAMPLE,
  PERSONNEL_PLANNING_FORM_TEMPLATE,
  COMPANY_FORM_TEMPLATE,
  APPLICATION_PROPERTY;

  public String getMessageKey() {
    return "definition.type." + name().toLowerCase(java.util.Locale.ENGLISH).replace("_", "-");
  }
}
