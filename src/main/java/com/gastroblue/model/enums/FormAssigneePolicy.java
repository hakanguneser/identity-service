package com.gastroblue.model.enums;

import com.gastro.formflow.services.util.enums.IDisplayableEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FormAssigneePolicy implements IDisplayableEnum {
  GROUP_BASE,
  TASK_BASE
}
