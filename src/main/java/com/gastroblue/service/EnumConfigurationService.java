package com.gastroblue.service;

import com.gastroblue.exception.IllegalDefinitionException;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnumConfigurationService {

  private final EnumValueConfigurationRepository repository;
  private final CacheManager cacheManager;

  /**
   * Two-tier lookup: loads global defaults (companyGroupId=null) and company-group overrides in one
   * query, merges them (override wins), then filters to active-only entries.
   */
  @Transactional
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

  public boolean isActive(
      String companyGroupId, String enumType, String enumKey, Language language) {
    return getDropdownValues(enumType, companyGroupId, language).stream()
        .anyMatch(r -> r.getKey().equals(enumKey));
  }

  /**
   * Validates that {@code enumKey} is a known, active value for the given enum type and company
   * group.
   *
   * <p>Merge logic mirrors {@link #getDropdownValues}: global defaults ({@code companyGroupId =
   * null}) are loaded first, then company-group-specific overrides replace them. The resolved entry
   * for the key is checked for {@code isActive}.
   *
   * <p>If no entry exists at all the key is <em>auto-registered</em> as a new global default
   * ({@code active = true}, {@code label = enumKey}). This "lazy seeding" means the first time any
   * value appears in a request it becomes a known value that admins can later label / deactivate.
   *
   * @return {@code true} if valid (or just auto-inserted), {@code false} if explicitly deactivated
   */
  @Transactional
  public boolean validateOrInsert(
      String enumType, String enumKey, String companyGroupId, Language language) {

    List<EnumValueConfigurationEntity> rows =
        repository.findForGroupWithDefaults(enumType, companyGroupId, language);

    // Same two-tier merge as getDropdownValues: global first, company-group overrides
    LinkedHashMap<String, EnumValueConfigurationEntity> merged = new LinkedHashMap<>();
    rows.stream()
        .filter(e -> e.getCompanyGroupId() == null)
        .forEach(e -> merged.put(e.getEnumKey(), e));
    rows.stream()
        .filter(e -> e.getCompanyGroupId() != null)
        .forEach(e -> merged.put(e.getEnumKey(), e));

    EnumValueConfigurationEntity resolved = merged.get(enumKey);
    if (resolved != null) {
      return resolved.isActive(); // explicitly deactivated → false
    }

    // Unknown key — auto-register as a new global default
    repository.save(
        EnumValueConfigurationEntity.builder()
            .companyGroupId(null)
            .enumType(enumType)
            .enumKey(enumKey)
            .language(language)
            .label(enumKey) // raw key as default label; admins can rename later
            .active(true)
            .build());

    // Evict cache programmatically — avoids Spring AOP self-invocation limitation
    Cache cache = cacheManager.getCache("enum_dropdown_configs");
    if (cache != null) {
      cache.clear();
    }

    return true;
  }

  @Transactional
  @Cacheable(
      value = "enum_dropdown_configs",
      key = "{#enumType, #companyGroupId, #sessionLanguage, #parentKey}")
  public List<ResolvedEnum> getChildDropdownValues(
      String enumType,
      String companyGroupId,
      Language sessionLanguage,
      String parentKey,
      String parentEnumType) {
    List<EnumValueConfigurationEntity> rows =
        repository.findChildrenWithDefaults(
            enumType, companyGroupId, sessionLanguage, parentKey, parentEnumType);

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
            .parentKey(request.parentKey())
            .parentEnumType(request.parentEnumType())
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
