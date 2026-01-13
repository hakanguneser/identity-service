package com.gastroblue.mapper;

import static com.gastroblue.util.DelimitedStringUtil.join;
import static com.gastroblue.util.DelimitedStringUtil.split;

import com.gastroblue.model.base.Company;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanySaveRequest;
import com.gastroblue.model.response.AuthUserCompanyGroupResponse;
import com.gastroblue.model.response.AuthUserCompanyResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompanyGroupMapper {

  public static CompanyGroupEntity toEntity(CompanyGroupSaveRequest request) {
    return CompanyGroupEntity.builder()
        .name(request.name())
        .groupCode(request.groupCode())
        .groupMail(join(request.groupMails()))
        .logoUrl(request.logoUrl())
        .build();
  }

  public static CompanyGroupDefinitionResponse toResponse(CompanyGroupEntity companyGroupEntity) {
    return CompanyGroupDefinitionResponse.builder()
        .companyGroupId(companyGroupEntity.getId())
        .groupCode(companyGroupEntity.getGroupCode())
        .name(companyGroupEntity.getName())
        .groupMails(split(companyGroupEntity.getGroupMail()))
        .logoUrl(companyGroupEntity.getLogoUrl())
        .build();
  }

  public static AuthUserCompanyGroupResponse toAuthResponse(CompanyGroupEntity companyGroupEntity) {
    return AuthUserCompanyGroupResponse.builder()
        .companyGroupId(companyGroupEntity.getId())
        .groupCode(companyGroupEntity.getGroupCode())
        .name(companyGroupEntity.getName())
        .groupMails(split(companyGroupEntity.getGroupMail()))
        .logoUrl(companyGroupEntity.getLogoUrl())
        .build();
  }

  public static CompanyEntity toEntity(
      final CompanySaveRequest companyRequest, final String companyGroupId) {
    return CompanyEntity.builder()
        .companyCode(companyRequest.companyCode())
        .companyName(companyRequest.companyName())
        .companyGroupId(companyGroupId)
        .companyMail(join(companyRequest.companyMail()))
        .country(companyRequest.country())
        .city(companyRequest.city())
        .zone(companyRequest.zone())
        .segment1(companyRequest.segment1())
        .segment2(companyRequest.segment2())
        .segment3(companyRequest.segment3())
        .segment4(companyRequest.segment4())
        .segment5(companyRequest.segment5())
        .active(companyRequest.isActive())
        .build();
  }

  public static CompanyDefinitionResponse toResponse(final CompanyEntity entity) {
    return CompanyDefinitionResponse.builder()
        .companyId(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(split(entity.getCompanyMail()))
        .city(entity.getCity())
        .country(entity.getCountry())
        .zone(entity.getZone())
        .segment1(entity.getSegment1())
        .segment2(entity.getSegment2())
        .segment3(entity.getSegment3())
        .segment4(entity.getSegment4())
        .segment5(entity.getSegment5())
        .isActive(entity.isActive())
        .build();
  }

  public static AuthUserCompanyResponse toAuthResponse(final CompanyEntity entity) {
    return AuthUserCompanyResponse.builder()
        .companyId(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(split(entity.getCompanyMail()))
        .city(entity.getCity())
        .country(entity.getCountry())
        .zone(entity.getZone())
        .segment1(entity.getSegment1())
        .segment2(entity.getSegment2())
        .segment3(entity.getSegment3())
        .segment4(entity.getSegment4())
        .segment5(entity.getSegment5())
        .isActive(entity.isActive())
        .build();
  }

  public static Company toBase(CompanyEntity companyEntity) {
    return Company.builder()
        .companyId(companyEntity.getId())
        .companyGroupId(companyEntity.getCompanyGroupId())
        .companyCode(companyEntity.getCompanyCode())
        .companyName(companyEntity.getCompanyName())
        .companyMail(split(companyEntity.getCompanyMail()))
        .city(companyEntity.getCity())
        .country(companyEntity.getCountry())
        .zone(companyEntity.getZone())
        .segment1(companyEntity.getSegment1())
        .segment2(companyEntity.getSegment2())
        .segment3(companyEntity.getSegment3())
        .segment4(companyEntity.getSegment4())
        .segment5(companyEntity.getSegment5())
        .isActive(companyEntity.isActive())
        .build();
  }

  public static CompanyGroup toBase(CompanyGroupEntity entity) {
    return CompanyGroup.builder()
        .companyGroupId(entity.getId())
        .name(entity.getName())
        .groupCode(entity.getGroupCode())
        .groupMails(split(entity.getGroupMail()))
        .logoUrl(entity.getLogoUrl())
        .build();
  }
}
