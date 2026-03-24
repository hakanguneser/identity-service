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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnumConfigurationService {

  private final EnumValueConfigurationRepository repository;

  /**
   * Two-tier lookup: loads global defaults (companyGroupId=null) and company-group overrides in one
   * query, merges them (override wins), then filters to active-only entries.
   *
   * <p>If no global defaults exist for this enum type and language, they are <b>auto-seeded</b>
   * from the enum's own constants using {@link ConfigurableEnum#getDefaultLabel()} so callers
   * never receive an empty dropdown on first use.
   */
  @Transactional
  @Cacheable(
      value = "enum_dropdown_configs",
      key = "{#enumClass.simpleName, #companyGroupId, #sessionLanguage}")
  public <T extends ConfigurableEnum> List<ResolvedEnum> getDropdownValues(
      Class<T> enumClass, final String companyGroupId, Language sessionLanguage) {

    String enumType = enumClass.getSimpleName();
    List<EnumValueConfigurationEntity> rows =
        repository.findForGroupWithDefaults(enumType, companyGroupId, sessionLanguage);

    boolean hasGlobalDefaults = rows.stream().anyMatch(e -> e.getCompanyGroupId() == null);

    if (!hasGlobalDefaults) {
      rows = seedGlobalDefaults(enumClass, enumType, sessionLanguage);
    }

    LinkedHashMap<String, EnumValueConfigurationEntity> merged = new LinkedHashMap<>();
    rows.stream()
        .filter(e -> e.getCompanyGroupId() == null)
        .forEach(e -> merged.put(e.getEnumKey(), e));
    rows.stream()
        .filter(e -> e.getCompanyGroupId() != null)
        .forEach(e -> merged.put(e.getEnumKey(), e));

    return merged.values().stream()
        .filter(EnumValueConfigurationEntity::isActive)
        .sorted(Comparator.comparingInt(e -> Optional.ofNullable(e.getDisplayOrder()).orElse(99)))
        .map(
            e ->
                ResolvedEnum.builder()
                    .key(e.getEnumKey())
                    .display(e.getLabel())
                    .displayOrder(e.getDisplayOrder())
                    .build())
        .toList();
  }

  private <T extends ConfigurableEnum> List<EnumValueConfigurationEntity> seedGlobalDefaults(
      Class<T> enumClass, String enumType, Language language) {
    T[] constants = enumClass.getEnumConstants();
    List<EnumValueConfigurationEntity> defaults = new ArrayList<>(constants.length);
    for (int i = 0; i < constants.length; i++) {
      T constant = constants[i];
      defaults.add(
          EnumValueConfigurationEntity.builder()
              .companyGroupId(null)
              .enumType(enumType)
              .enumKey(constant.name())
              .language(language)
              .label(constant.getDefaultLabel())
              .active(true)
              .displayOrder(i + 1)
              .build());
    }
    return repository.saveAll(defaults);
  }

  public boolean isActive(String companyGroupId, ConfigurableEnum enumValue, Language language) {
    return getDropdownValues(enumValue.getClass(), companyGroupId, language).stream()
        .anyMatch(r -> r.getKey().equals(enumValue.name()));
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
  @CacheEvict(value = "enum_dropdown_configs", allEntries = true)
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
  @CacheEvict(value = "enum_dropdown_configs", allEntries = true)
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
    return repository.save(entity);
  }

  @Transactional(readOnly = true)
  public List<EnumValueConfigurationEntity> findAll(String companyGroupId) {
    return repository.findByCompanyGroupId(companyGroupId);
  }
}
