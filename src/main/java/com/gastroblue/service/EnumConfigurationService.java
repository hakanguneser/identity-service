package com.gastroblue.service;

import com.gastroblue.exception.ValidationException;
import com.gastroblue.model.base.ConfigurableEnum;
import com.gastroblue.model.base.GlobalConfigurableEnum;
import com.gastroblue.model.entity.EnumValueConfiguration;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.request.EnumConfigurationSaveRequest;
import com.gastroblue.model.request.EnumConfigurationUpdateRequest;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.repository.EnumValueConfigurationRepository;
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
  public <T extends ConfigurableEnum> List<ResolvedEnum<T>> getDropdownValues(
      Class<T> enumClass, String companyGroupId) {
    if (GlobalConfigurableEnum.class.isAssignableFrom(enumClass)) {
      companyGroupId = "*";
    }
    String enumType = enumClass.getSimpleName();
    Language sessionLanguage = IJwtService.getSessionLanguage();

    // 1. Fetch existing configs
    List<EnumValueConfiguration> existingConfigs =
        repository.findByCompanyGroupIdAndEnumTypeAndLanguage(
            companyGroupId, enumType, sessionLanguage);
    Map<String, EnumValueConfiguration> configMap =
        existingConfigs.stream()
            .collect(Collectors.toMap(EnumValueConfiguration::getEnumKey, config -> config));

    List<ResolvedEnum<T>> options = new ArrayList<>();
    T[] enumConstants = enumClass.getEnumConstants();

    if (enumConstants == null) {
      return options;
    }

    // 2. Iterate and resolve
    for (T enumConstant : enumConstants) {
      String key = enumConstant.name();
      EnumValueConfiguration config = configMap.get(key);

      if (config == null) {
        // 3. Create default if missing
        String defaultLabel = String.format("%s-%s", key, sessionLanguage);
        config =
            EnumValueConfiguration.builder()
                .companyGroupId(companyGroupId)
                .enumType(enumType)
                .enumKey(key)
                .language(sessionLanguage)
                .label(defaultLabel)
                .active(true)
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
      key =
          "{#enumValue.getClass().getSimpleName(), #enumValue.name(), #companyGroupId, #language}")
  public <T extends ConfigurableEnum> ResolvedEnum<T> resolve(
      T enumValue, String companyGroupId, String language) {
    if (enumValue == null) {
      return null;
    }

    if (enumValue instanceof GlobalConfigurableEnum) {
      companyGroupId = "*";
    }

    String enumType = enumValue.getClass().getSimpleName();
    String key = enumValue.name();
    Language sessionLanguage = IJwtService.getSessionLanguage();

    EnumValueConfiguration config =
        repository
            .findByCompanyGroupIdAndEnumTypeAndLanguage(companyGroupId, enumType, sessionLanguage)
            .stream()
            .filter(c -> c.getEnumKey().equals(key))
            .findFirst()
            .orElse(null);

    if (config == null) {
      String defaultLabel = String.format("%s-%s", key, language);
      config =
          EnumValueConfiguration.builder()
              .companyGroupId(companyGroupId)
              .enumType(enumType)
              .enumKey(key)
              .language(sessionLanguage)
              .label(defaultLabel)
              .active(true)
              .build();
      config = repository.save(config);
    }

    return ResolvedEnum.<T>builder().key(enumValue).display(config.getLabel()).build();
  }

  @Transactional(readOnly = true)
  public EnumValueConfiguration findById(String id, String companyGroupId) {
    EnumValueConfiguration entity =
        repository
            .findById(id)
            .orElseThrow(() -> new ValidationException(ErrorCode.CONFIGURATION_NOT_FOUND));

    if (!entity.getCompanyGroupId().equals("*")
        && !entity.getCompanyGroupId().equals(companyGroupId)) {
      throw new ValidationException(ErrorCode.COMPANY_MISMATCH);
    }

    return entity;
  }

  @Transactional
  @CacheEvict(value = "enum_configs", allEntries = true)
  public EnumValueConfiguration save(EnumConfigurationSaveRequest request) {
    String finalCompanyGroupId = request.companyGroupId();
    if (isGlobal(request.enumType())) {
      finalCompanyGroupId = "*";
    }

    EnumValueConfiguration entity =
        EnumValueConfiguration.builder()
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
  public EnumValueConfiguration update(
      String id, EnumConfigurationUpdateRequest request, String companyGroupId) {
    EnumValueConfiguration entity = findById(id, companyGroupId);
    if (request.label() != null) {
      entity.setLabel(request.label());
    }
    if (request.active() != null) {
      entity.setActive(request.active());
    }
    return repository.save(entity);
  }

  @Transactional(readOnly = true)
  public List<EnumValueConfiguration> findAll(String companyGroupId) {
    return repository.findByCompanyGroupId(companyGroupId);
  }

  private boolean isGlobal(String enumType) {
    try {
      Class<?> clazz = Class.forName("com.gastroblue.model.enums." + enumType);
      return GlobalConfigurableEnum.class.isAssignableFrom(clazz);
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
