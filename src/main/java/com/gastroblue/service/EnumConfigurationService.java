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
   */
  @Cacheable(
      value = "enum_dropdown_configs",
      key = "{#enumType, #companyGroupId, #sessionLanguage}")
  public List<ResolvedEnum> getDropdownValues(
      String enumType, final String companyGroupId, Language sessionLanguage) {

    List<EnumValueConfigurationEntity> rows =
        repository.findForGroupWithDefaults(enumType, companyGroupId, sessionLanguage);

    LinkedHashMap<String, EnumValueConfigurationEntity> merged = new LinkedHashMap<>();
    rows.stream()
        .filter(e -> e.getCompanyGroupId() == null)
        .forEach(e -> merged.put(e.getEnumKey(), e));
    rows.stream()
        .filter(e -> e.getCompanyGroupId() != null)
        .forEach(e -> merged.put(e.getEnumKey(), e));

    return merged.values().stream()
        .filter(EnumValueConfigurationEntity::isActive)
        .sorted(
            Comparator.comparingInt(e -> Optional.ofNullable(e.getDisplayOrder()).orElse(99)))
        .map(
            e ->
                ResolvedEnum.builder()
                    .key(e.getEnumKey())
                    .display(e.getLabel())
                    .displayOrder(e.getDisplayOrder())
                    .build())
        .toList();
  }

  public boolean isActive(String companyGroupId, ConfigurableEnum enumValue, Language language) {
    return getDropdownValues(enumValue.getClass().getSimpleName(), companyGroupId, language)
        .stream()
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
