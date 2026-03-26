package com.gastroblue.annotations.validation.field.enumkey;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.Language;
import com.gastroblue.service.EnumConfigurationService;
import com.gastroblue.service.IJwtService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validates that the string value is a known, active enum key for the caller's company group.
 *
 * <p>Resolves company group and language from the current {@link
 * com.gastroblue.model.base.SessionUser} and delegates to {@link
 * EnumConfigurationService#isActive}. Unknown keys (not yet defined in {@code
 * ENUM_VALUE_CONFIGURATIONS}) and explicitly deactivated keys are both rejected.
 *
 * <p>Spring Boot's {@code LocalValidatorFactoryBean} manages validator instances through {@code
 * SpringConstraintValidatorFactory}, so {@code @Autowired} injection works here.
 */
public class EnumKeyValidator implements ConstraintValidator<ValidEnumKey, String> {

  @Autowired private EnumConfigurationService enumConfigurationService;

  private String enumType;
  private boolean productScoped;

  @Override
  public void initialize(ValidEnumKey annotation) {
    this.enumType = annotation.enumType();
    this.productScoped = annotation.productScoped();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // null / blank checked by @NotNull / @NotBlank separately
    }

    SessionUser sessionUser = IJwtService.findSessionUser();
    String companyGroupId = sessionUser != null ? sessionUser.companyGroupId() : null;
    Language language = IJwtService.getSessionLanguage();

    if (productScoped) {
      ApplicationProduct product =
          sessionUser != null ? sessionUser.getApplicationProduct() : null;
      return product != null
          && enumConfigurationService.isActive(companyGroupId, enumType, value, language, product);
    }

    return enumConfigurationService.isActive(companyGroupId, enumType, value, language);
  }
}
