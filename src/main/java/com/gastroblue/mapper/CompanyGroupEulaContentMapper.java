package com.gastroblue.mapper;

import com.gastroblue.model.entity.CompanyGroupEulaContentEntity;
import com.gastroblue.model.request.CompanyGroupEulaContentSaveRequest;
import com.gastroblue.model.response.CompanyGroupEulaContentResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CompanyGroupEulaContentMapper {

  public static CompanyGroupEulaContentEntity toEntity(
      String companyGroupId, CompanyGroupEulaContentSaveRequest request) {
    return CompanyGroupEulaContentEntity.builder()
        .companyGroupId(companyGroupId)
        .eulaVersion(request.eulaVersion())
        .language(request.language())
        .content(request.content())
        .startDate(request.startDate())
        .endDate(request.endDate())
        .build();
  }

  public static CompanyGroupEulaContentResponse toResponse(CompanyGroupEulaContentEntity entity) {
    return CompanyGroupEulaContentResponse.builder()
        .id(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .eulaVersion(entity.getEulaVersion())
        .language(entity.getLanguage())
        .content(entity.getContent())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .createdAt(entity.getCreatedDate())
        .updatedAt(entity.getLastModifiedDate())
        .build();
  }
}
