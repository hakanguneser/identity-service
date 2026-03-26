package com.gastroblue.facade;

import com.gastroblue.model.entity.EnumValueConfigurationEntity;
import com.gastroblue.model.request.EnumConfigurationSaveRequest;
import com.gastroblue.model.request.EnumConfigurationUpdateRequest;
import com.gastroblue.model.response.EnumConfigurationResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.EnumConfigurationService;
import com.gastroblue.service.IJwtService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnumConfigurationFacade {

  private final EnumConfigurationService enumConfigurationService;

  public EnumConfigurationResponse save(EnumConfigurationSaveRequest request) {
    return toResponse(enumConfigurationService.save(request));
  }

  public EnumConfigurationResponse update(
      String id, EnumConfigurationUpdateRequest request, String companyGroupId) {
    return toResponse(enumConfigurationService.update(id, request, companyGroupId));
  }

  public List<ResolvedEnum> getDropdownValues(String enumType, String companyGroupId) {
    return enumConfigurationService.getDropdownValues(
        enumType, companyGroupId, IJwtService.getSessionLanguage());
  }

  public List<ResolvedEnum> getDropdownValues(String enumType) {
    return getDropdownValues(enumType, IJwtService.findSessionUserOrThrow().companyGroupId());
  }

  public EnumConfigurationResponse findById(String id, String companyGroupId) {
    return toResponse(enumConfigurationService.findByIdAndCompanyGroupId(id, companyGroupId));
  }

  public List<EnumConfigurationResponse> findAll(String companyGroupId) {
    return enumConfigurationService.findAll(companyGroupId).stream().map(this::toResponse).toList();
  }

  public ResolvedEnum resolve(String enumType, String enumKey, String companyGroupId) {
    if (enumKey == null) {
      return null;
    }
    return getDropdownValues(enumType, companyGroupId).stream()
        .filter(resolved -> Objects.equals(resolved.getKey(), enumKey))
        .findFirst()
        .orElse(null);
  }

  public List<ResolvedEnum> getChildDropdownValues(
      String enumType, String parentEnumType, String parentKey, String companyGroupId) {
    return enumConfigurationService.getChildDropdownValues(
        enumType, companyGroupId, IJwtService.getSessionLanguage(), parentKey, parentEnumType);
  }

  private EnumConfigurationResponse toResponse(EnumValueConfigurationEntity entity) {
    return EnumConfigurationResponse.builder()
        .id(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .enumType(entity.getEnumType())
        .enumKey(entity.getEnumKey())
        .language(entity.getLanguage())
        .label(entity.getLabel())
        .active(entity.isActive())
        .displayOrder(entity.getDisplayOrder())
        .parentKey(entity.getParentKey())
        .parentEnumType(entity.getParentEnumType())
        .build();
  }
}
