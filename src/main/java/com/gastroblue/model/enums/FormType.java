package com.gastroblue.model.enums;

import com.gastroblue.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FormType {
  PERSONNEL,
  ACTIVITY,
  CHECKLIST,
  ASSET_MEASUREMENT,
  WITNESS_SAMPLE;

  public void checkFormType(FormType formType) {
    if (this != formType) {
      throw new ValidationException(ErrorCode.INVALID_FORM_TYPE);
    }
  }
}
