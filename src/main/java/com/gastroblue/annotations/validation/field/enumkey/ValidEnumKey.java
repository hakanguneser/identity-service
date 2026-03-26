package com.gastroblue.annotations.validation.field.enumkey;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a string value is a recognized, active enum key for the caller's company group.
 *
 * <p>The company group and language are resolved automatically from the current {@link
 * com.gastroblue.model.base.SessionUser}. If the key does not exist yet in {@code
 * ENUM_VALUE_CONFIGURATIONS} it is auto-registered as a global default (active, label = key) so
 * first-time values are never rejected. If the key exists but is explicitly deactivated the field
 * is rejected.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @ValidEnumKey(enumType = EnumTypes.CITY)
 * String city;
 *
 * List<@ValidEnumKey(enumType = EnumTypes.DEPARTMENT) String> departments;
 * }</pre>
 */
@Documented
@Constraint(validatedBy = EnumKeyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnumKey {

  String enumType();

  /**
   * When {@code true} the validator fetches the {@link
   * com.gastroblue.model.enums.ApplicationProduct} from the current session and passes it to the
   * product-scoped {@code isActive} check. Use for enums whose valid values differ per product
   * (e.g. Department). Defaults to {@code false} (product-agnostic check).
   */
  boolean productScoped() default false;

  String message() default "{validation.enum.key.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
