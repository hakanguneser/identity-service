package com.gastroblue.model.enums;

import com.gastroblue.util.enums.IDisplayableEnum;

public enum DefinitionType implements IDisplayableEnum {
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

  @Override
  public String getMessageKey() {
    return "definition.type." + getEnumCode();
  }
}
