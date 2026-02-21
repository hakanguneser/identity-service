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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnumConfigurationService {

  private final EnumValueConfigurationRepository repository;

  @Cacheable(
      value = "enum_dropdown_configs",
      key = "{#enumType, #companyGroupId, #sessionLanguage}")
  public List<ResolvedEnum> getDropdownValues(
      String enumType, final String companyGroupId, Language sessionLanguage) {
    return repository
        .findByEnumTypeAndCompanyGroupIdAndLanguage(enumType, companyGroupId, sessionLanguage)
        .stream()
        .map(
            e ->
                ResolvedEnum.builder()
                    .key(e.getEnumKey())
                    .display(e.getLabel())
                    .displayOrder(e.getDisplayOrder())
                    .build())
        .toList();
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
