package com.gastroblue.facade;

import com.gastroblue.model.entity.EnumValueConfiguration;
import com.gastroblue.model.request.EnumConfigurationSaveRequest;
import com.gastroblue.model.request.EnumConfigurationUpdateRequest;
import com.gastroblue.model.response.EnumConfigurationResponse;
import com.gastroblue.service.EnumConfigurationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnumConfigurationFacade {

  private final EnumConfigurationService service;

  public EnumConfigurationResponse save(EnumConfigurationSaveRequest request) {
    return toResponse(service.save(request));
  }

  public EnumConfigurationResponse update(
      String id, EnumConfigurationUpdateRequest request, String companyGroupId) {
    return toResponse(service.update(id, request, companyGroupId));
  }

  public EnumConfigurationResponse findById(String id, String companyGroupId) {
    return toResponse(service.findById(id, companyGroupId));
  }

  public List<EnumConfigurationResponse> findAll(String companyGroupId) {
    return service.findAll(companyGroupId).stream().map(this::toResponse).toList();
  }

  private EnumConfigurationResponse toResponse(EnumValueConfiguration entity) {
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
}
