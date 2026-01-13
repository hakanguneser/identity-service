package com.gastroblue.annotations.validation.field.unique;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueFieldValidator implements ConstraintValidator<UniqueField, List<?>> {

  private String fieldName;

  @Override
  public void initialize(UniqueField constraintAnnotation) {
    this.fieldName = constraintAnnotation.fieldName();
  }

  @Override
  public boolean isValid(List<?> value, ConstraintValidatorContext context) {
    if (value == null) return true;

    Set<Object> seen = new HashSet<>();

    for (Object item : value) {
      try {
        Field field = item.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object fieldValue = field.get(item);

        if (fieldValue != null && !seen.add(fieldValue)) {
          return false; // duplicate bulundu
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        // Eğer field bulunmazsa validasyonu bozma -> annotation yanlış kullanılmıştır
        return false;
      }
    }

    return true;
  }
}
