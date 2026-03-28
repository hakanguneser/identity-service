package com.gastroblue.annotations.validation.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Class-level constraint for {@link com.gastroblue.model.request.UserSaveRequest}.
 *
 * <p>Validates each department string in the request against the {@code ENUM_VALUE_CONFIGURATIONS}
 * table using the {@code product} field declared in the <em>same request</em> — not the caller's
 * session product. This is necessary because an ADMIN may create users for a product different from
 * their own session context.
 */
@Documented
@Constraint(validatedBy = DepartmentsForProductValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDepartmentsForProduct {

  String message() default "{validation.departments.invalid_for_product}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
