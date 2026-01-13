package com.gastroblue.annotations.validation.field.code;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ItemCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidItemCode {

  String message() default "{validation.itemCode.check.pattern}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
