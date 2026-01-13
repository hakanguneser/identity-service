package com.gastroblue.annotations.validation.field.phone;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
  String message() default "{validation.phone.number}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
