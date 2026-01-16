package com.gastroblue.service;

import com.gastroblue.model.base.ConfigurableEnum;
import com.gastroblue.model.entity.EnumValueConfiguration;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.repository.EnumValueConfigurationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
}
