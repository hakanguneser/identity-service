package com.gastroblue.annotations.validation.field.unique;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueFieldValidator.class)
public @interface UniqueField {

  /** Tekil olması gereken alanın adı (record/class içinde). */
  String fieldName();

  String message() default "Duplicate value found for field: {fieldName}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
