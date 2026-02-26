package com.gastroblue.facade;

import com.gastroblue.model.base.ConfigurableEnum;
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

  public <T extends ConfigurableEnum> List<ResolvedEnum> getDropdownValues(
      Class<T> enumClass, String companyGroupId) {
    return enumConfigurationService.getDropdownValues(
        enumClass.getSimpleName(), companyGroupId, IJwtService.getSessionLanguage());
  }

  public <T extends ConfigurableEnum> List<ResolvedEnum> getDropdownValues(Class<T> enumClass) {
    return getDropdownValues(enumClass, IJwtService.findSessionUserOrThrow().companyGroupId());
  }

  public EnumConfigurationResponse findById(String id, String companyGroupId) {
    return toResponse(enumConfigurationService.findByIdAndCompanyGroupId(id, companyGroupId));
  }

  public List<EnumConfigurationResponse> findAll(String companyGroupId) {
    return enumConfigurationService.findAll(companyGroupId).stream().map(this::toResponse).toList();
  }

  public <T extends ConfigurableEnum> ResolvedEnum resolve(
      T enumValue, final String companyGroupId) {
    return getDropdownValues(enumValue.getClass(), companyGroupId).stream()
        .filter(resolved -> Objects.equals(resolved.getKey(), enumValue.name()))
        .findFirst()
        .orElse(null);
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

  public void copyConfigurations(String toCompanyGroupId) {
    enumConfigurationService.copyConfigurations(toCompanyGroupId);
  }
}
