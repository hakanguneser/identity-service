package com.gastroblue.service;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.model.base.ConfigurableEnum;
import com.gastroblue.model.base.DefaultConfigurableEnum;
import com.gastroblue.model.entity.EnumValueConfigurationEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.request.EnumConfigurationSaveRequest;
import com.gastroblue.model.request.EnumConfigurationUpdateRequest;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.repository.EnumValueConfigurationRepository;
import com.gastroblue.service.impl.CompanyGroupService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnumConfigurationService {

  private final EnumValueConfigurationRepository repository;

  @Transactional
  public <T extends DefaultConfigurableEnum> List<ResolvedEnum<T>> getDropdownValues(
      Class<T> enumClass, final String companyGroupId) {

    String enumType = enumClass.getSimpleName();
    Language sessionLanguage = IJwtService.getSessionLanguage();

    // 1. Fetch existing configs
    List<EnumValueConfigurationEntity> existingConfigs =
        repository.findByCompanyGroupIdAndEnumTypeAndLanguage(
            companyGroupId, enumType, sessionLanguage);
    Map<String, EnumValueConfigurationEntity> configMap =
        existingConfigs.stream()
            .collect(Collectors.toMap(EnumValueConfigurationEntity::getEnumKey, config -> config));

    List<ResolvedEnum<T>> options = new ArrayList<>();
    T[] enumConstants = enumClass.getEnumConstants();

    if (enumConstants == null) {
      return options;
    }

    // 2. Iterate and resolve
    for (T enumConstant : enumConstants) {
      String key = enumConstant.name();
      EnumValueConfigurationEntity config = configMap.get(key);

      if (config == null) {
        // 3. Create default if missing
        String defaultLabel = String.format("%s-%s", key, sessionLanguage);
        config =
            EnumValueConfigurationEntity.builder()
                .companyGroupId(companyGroupId)
                .enumType(enumType)
                .enumKey(key)
                .language(sessionLanguage)
                .label(defaultLabel)
                .active(getDefaultStatus(enumClass))
                .build();
        config = repository.save(config);
      }

      // 4. Filter inactive
      if (config.isActive()) {
        options.add(ResolvedEnum.<T>builder().key(enumConstant).display(config.getLabel()).build());
      }
    }

    return options;
  }

  @Transactional
  @Cacheable(
      value = "enum_configs",
      key = "{#enumValue.getClass().getSimpleName(), #companyGroupId, #language}")
  public <T extends DefaultConfigurableEnum> ResolvedEnum<T> resolve(
      T enumValue, final String companyGroupId, Language language) {
    String enumType = enumValue.getClass().getSimpleName();
    String key = enumValue.name();

    EnumValueConfigurationEntity config =
        repository
            .findByCompanyGroupIdAndEnumTypeAndLanguage(companyGroupId, enumType, language)
            .stream()
            .filter(c -> c.getEnumKey().equals(key))
            .findFirst()
            .orElse(null);

    if (config == null) {
      String defaultLabel = String.format("%s-%s", key, language);
      config =
          EnumValueConfigurationEntity.builder()
              .companyGroupId(companyGroupId)
              .enumType(enumType)
              .enumKey(key)
              .language(language)
              .label(defaultLabel)
              .active(true)
              .build();
      config = repository.save(config);
    }

    return ResolvedEnum.<T>builder().key(enumValue).display(config.getLabel()).build();
  }

  @Transactional(readOnly = true)
  public EnumValueConfigurationEntity findByIdAndCompanyGroupId(String id, String companyGroupId) {
    return repository
        .findByIdAndCompanyGroupId(id, companyGroupId)
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    ErrorCode.ENUM_CONFIGURATION_NOT_FOUND,
                    String.format(
                        "Enum Configuration not found (id=%s, companyGroupId=%s)",
                        id, companyGroupId)));
  }

  @Transactional
  @CacheEvict(value = "enum_configs", allEntries = true)
  public EnumValueConfigurationEntity save(EnumConfigurationSaveRequest request) {
    String finalCompanyGroupId =
        request.companyGroupId() == null
            ? CompanyGroupService.DEFAULT_COMPANY_GROUP_ID
            : request.companyGroupId();
    EnumValueConfigurationEntity entity =
        EnumValueConfigurationEntity.builder()
            .companyGroupId(finalCompanyGroupId)
            .enumType(request.enumType())
            .enumKey(request.enumKey())
            .language(request.language())
            .label(request.label())
            .active(request.active())
            .build();
    return repository.save(entity);
  }

  @Transactional
  @CacheEvict(value = "enum_configs", allEntries = true)
  public EnumValueConfigurationEntity update(
      String id, EnumConfigurationUpdateRequest request, String companyGroupId) {
    EnumValueConfigurationEntity entity =
        repository
            .findByIdAndCompanyGroupId(id, companyGroupId)
            .orElseThrow(
                () ->
                    new IllegalDefinitionException(
                        ErrorCode.ENUM_CONFIGURATION_NOT_FOUND,
                        String.format(
                            "Enum Configuration not found (id=%s, companyGroupId=%s)",
                            id, companyGroupId)));
    if (request.label() != null) {
      entity.setLabel(request.label());
    }
    if (request.active() != null) {
      entity.setActive(request.active());
    }
    return repository.save(entity);
  }

  @Transactional(readOnly = true)
  public List<EnumValueConfigurationEntity> findAll(String companyGroupId) {
    if (companyGroupId == null) {
      companyGroupId = CompanyGroupService.DEFAULT_COMPANY_GROUP_ID;
    }
    return repository.findByCompanyGroupId(companyGroupId);
  }

  private boolean getDefaultStatus(Class<? extends DefaultConfigurableEnum> enumClass) {
    return isDefaultEnum(enumClass);
  }

  public boolean isDefaultEnum(Class<? extends DefaultConfigurableEnum> enumClass) {
    return !ConfigurableEnum.class.isAssignableFrom(enumClass);
  }
}
