package com.gastroblue.annotations.validation.field.code;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItemCodeValidator implements ConstraintValidator<ValidItemCode, String> {

  private static final String CODE_REGEX = "^[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*$";

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return false;
    }
    return value.matches(CODE_REGEX);
  }
}
