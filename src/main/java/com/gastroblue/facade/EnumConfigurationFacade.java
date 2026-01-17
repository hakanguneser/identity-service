package com.gastroblue.facade;

import com.gastroblue.model.base.DefaultConfigurableEnum;
import com.gastroblue.model.entity.EnumValueConfigurationEntity;
import com.gastroblue.model.request.EnumConfigurationSaveRequest;
import com.gastroblue.model.request.EnumConfigurationUpdateRequest;
import com.gastroblue.model.response.EnumConfigurationResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.EnumConfigurationService;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.CompanyGroupService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnumConfigurationFacade {

  private final EnumConfigurationService enumConfigurationService;
  private final CompanyGroupService companyGroupService;

  public EnumConfigurationResponse save(EnumConfigurationSaveRequest request) {
    return toResponse(enumConfigurationService.save(request));
  }

  public EnumConfigurationResponse update(
      String id, EnumConfigurationUpdateRequest request, String companyGroupId) {
    return toResponse(enumConfigurationService.update(id, request, companyGroupId));
  }

  public <T extends DefaultConfigurableEnum> List<ResolvedEnum<T>> getDropdownValues(
      Class<T> enumClass, String companyGroupId) {
    String finalCompanyGroupId = getCompanyGroupId(enumClass, companyGroupId);
    return enumConfigurationService.getDropdownValues(enumClass, finalCompanyGroupId);
  }

  public EnumConfigurationResponse findById(String id, String companyGroupId) {
    return toResponse(enumConfigurationService.findByIdAndCompanyGroupId(id, companyGroupId));
  }

  public List<EnumConfigurationResponse> findAll(String companyGroupId) {
    return enumConfigurationService.findAll(companyGroupId).stream().map(this::toResponse).toList();
  }

  public <T extends DefaultConfigurableEnum> ResolvedEnum<T> resolve(
      T enumValue, final String companyGroupId) {
    if (enumValue == null) {
      return null;
    }
    String finalCompanyGroupId = getCompanyGroupId(enumValue, companyGroupId);
    return enumConfigurationService.resolve(
        enumValue, finalCompanyGroupId, IJwtService.getSessionLanguage());
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
        .build();
  }

  private <T extends DefaultConfigurableEnum> String getCompanyGroupId(
      T enumValue, String companyGroupId) {
    boolean defaultEnum = enumValue.isDefault();
    if (defaultEnum) {
      return "*";
    } else {
      return companyGroupService.findByIdOrThrow(companyGroupId).getId();
    }
  }

  private <T extends DefaultConfigurableEnum> String getCompanyGroupId(
      Class<T> enumClass, String companyGroupId) {
    boolean defaultEnum = enumConfigurationService.isDefaultEnum(enumClass);
    if (defaultEnum) {
      return "*";
    } else {
      return companyGroupService.findByIdOrThrow(companyGroupId).getId();
    }
  }
}
