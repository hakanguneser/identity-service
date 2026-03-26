package com.gastroblue.annotations.validation.request;

import com.gastroblue.model.enums.EnumTypes;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.service.EnumConfigurationService;
import com.gastroblue.service.IJwtService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validates each department in {@link UserSaveRequest#departments()} against the product declared
 * in {@link UserSaveRequest#product()}.
 *
 * <p>Skips validation when either {@code departments} or {@code product} is null/empty so that
 * {@code @NotNull} field-level constraints handle the missing-value case.
 */
public class DepartmentsForProductValidator
    implements ConstraintValidator<ValidDepartmentsForProduct, UserSaveRequest> {

  @Autowired private EnumConfigurationService enumConfigurationService;

  @Override
  public boolean isValid(UserSaveRequest request, ConstraintValidatorContext context) {
    if (request == null) {
      return true;
    }

    List<String> departments = request.departments();
    if (departments == null || departments.isEmpty() || request.product() == null) {
      return true;
    }

    String companyGroupId = request.companyGroupId();

    boolean allValid =
        departments.stream()
            .filter(d -> d != null && !d.isBlank())
            .allMatch(
                dept ->
                    enumConfigurationService.isActive(
                        companyGroupId,
                        EnumTypes.DEPARTMENT,
                        dept,
                        IJwtService.getSessionLanguage(),
                        request.product()));

    if (!allValid) {
      // Point the violation to the departments field for a clearer error message
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
          .addPropertyNode("departments")
          .addConstraintViolation();
    }

    return allValid;
  }
}
