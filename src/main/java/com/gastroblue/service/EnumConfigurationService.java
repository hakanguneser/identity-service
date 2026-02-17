package com.gastroblue.service;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.model.base.ConfigurableEnum;
import com.gastroblue.model.entity.EnumValueConfigurationEntity;
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
  @Cacheable(
      value = "enum_dropdown_configs",
      key = "{#enumClass.getClass().getSimpleName(), #companyGroupId, #sessionLanguage}")
  public <T extends ConfigurableEnum> List<ResolvedEnum<T>> getDropdownValues(
      Class<T> enumClass, final String companyGroupId, Language sessionLanguage) {
    String enumType = enumClass.getSimpleName();
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
        int displayOrder = (enumConstant instanceof Enum) ? ((Enum<?>) enumConstant).ordinal() : 0;
        config =
            EnumValueConfigurationEntity.builder()
                .companyGroupId(companyGroupId)
                .enumType(enumType)
                .enumKey(key)
                .language(sessionLanguage)
                .label(defaultLabel)
                .active(true)
                .displayOrder(displayOrder)
                .build();
        config = repository.save(config);
      }

      // 4. Filter inactive
      if (config.isActive()) {
        options.add(
            ResolvedEnum.<T>builder()
                .key(enumConstant)
                .display(config.getLabel())
                .displayOrder(config.getDisplayOrder())
                .build());
      }
    }

    // 5. Sort by displayOrder
    options.sort(
        (o1, o2) -> {
          int order1 = o1.getDisplayOrder() != null ? o1.getDisplayOrder() : Integer.MAX_VALUE;
          int order2 = o2.getDisplayOrder() != null ? o2.getDisplayOrder() : Integer.MAX_VALUE;
          return Integer.compare(order1, order2);
        });

    return options;
  }

  @Transactional
  @Cacheable(
      value = "enum_resolved_configs",
      key = "{#enumValue.getClass().getSimpleName(), #companyGroupId, #language}")
  public <T extends ConfigurableEnum> ResolvedEnum<T> resolve(
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
      int displayOrder = (enumValue instanceof Enum) ? ((Enum<?>) enumValue).ordinal() : 0;
      config =
          EnumValueConfigurationEntity.builder()
              .companyGroupId(companyGroupId)
              .enumType(enumType)
              .enumKey(key)
              .language(language)
              .label(defaultLabel)
              .active(true)
              .displayOrder(displayOrder)
              .build();
      config = repository.save(config);
    }

    return ResolvedEnum.<T>builder()
        .key(enumValue)
        .display(config.getLabel())
        .displayOrder(config.getDisplayOrder())
        .build();
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
    EnumValueConfigurationEntity entity =
        EnumValueConfigurationEntity.builder()
            .companyGroupId(request.companyGroupId())
            .enumType(request.enumType())
            .enumKey(request.enumKey())
            .language(request.language())
            .label(request.label())
            .active(request.active())
            .displayOrder(request.displayOrder())
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
    if (request.displayOrder() != null) {
      entity.setDisplayOrder(request.displayOrder());
    }
    if (request.displayOrder() != null) {
      entity.setDisplayOrder(request.displayOrder());
    }
    return repository.save(entity);
  }

  @Transactional(readOnly = true)
  public List<EnumValueConfigurationEntity> findAll(String companyGroupId) {
    return repository.findByCompanyGroupId(companyGroupId);
  }

  public boolean isActive(String companyGroupId, ConfigurableEnum enumValue, Language language) {
    EnumValueConfigurationEntity config =
        repository
            .findByCompanyGroupIdAndEnumTypeAndLanguage(
                companyGroupId, enumValue.getClass().getSimpleName(), language)
            .stream()
            .filter(c -> c.getEnumKey().equals(enumValue.name()))
            .findFirst()
            .orElse(null);

    if (config == null) {
      return true;
    }
    return config.isActive();
  }

  @Transactional
  public void copyConfigurations(String toCompanyGroupId) {
    List<EnumValueConfigurationEntity> defaultConfigs = repository.findByCompanyGroupId(null);

    List<EnumValueConfigurationEntity> newConfigs =
        defaultConfigs.stream()
            .map(
                c ->
                    EnumValueConfigurationEntity.builder()
                        .companyGroupId(toCompanyGroupId)
                        .enumType(c.getEnumType())
                        .enumKey(c.getEnumKey())
                        .language(c.getLanguage())
                        .label(c.getLabel())
                        .active(false)
                        .displayOrder(c.getDisplayOrder())
                        .build())
            .toList();

    repository.saveAll(newConfigs);
  }
}
