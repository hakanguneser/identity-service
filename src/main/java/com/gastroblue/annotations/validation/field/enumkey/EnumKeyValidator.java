package com.gastroblue.annotations.validation.field.enumkey;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.enums.Language;
import com.gastroblue.service.EnumConfigurationService;
import com.gastroblue.service.IJwtService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Resolves the current session's company group and language, then delegates to {@link
 * EnumConfigurationService#validateOrInsert} which either confirms the key is active or
 * auto-registers it as a new global default.
 *
 * <p>Spring Boot's {@code LocalValidatorFactoryBean} manages validator instances through
 * {@code SpringConstraintValidatorFactory}, so {@code @Autowired} injection works here.
 */
public class EnumKeyValidator implements ConstraintValidator<ValidEnumKey, String> {

  @Autowired private EnumConfigurationService enumConfigurationService;

  private String enumType;

  @Override
  public void initialize(ValidEnumKey annotation) {
    this.enumType = annotation.enumType();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true; // null / blank checked by @NotNull / @NotBlank separately
    }

    SessionUser sessionUser = IJwtService.findSessionUser();
    String companyGroupId = sessionUser != null ? sessionUser.companyGroupId() : null;
    Language language = IJwtService.getSessionLanguage();

    return enumConfigurationService.validateOrInsert(enumType, value, companyGroupId, language);
  }
}
