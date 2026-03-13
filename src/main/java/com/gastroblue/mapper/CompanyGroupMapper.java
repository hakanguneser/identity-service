package com.gastroblue.mapper;

import static com.gastroblue.util.DelimitedStringUtil.join;
import static com.gastroblue.util.DelimitedStringUtil.split;

import com.gastroblue.facade.EnumConfigurationFacade;
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
import com.gastroblue.util.DelimitedStringUtil;
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
        .thermometerTrackerApiUrl(request.thermometerTrackerApiUrl())
        .thermometerTrackerApiVersion(request.thermometerTrackerApiVersion())
        .thermometerTrackerEnabled(
            request.thermometerTrackerEnabled() != null
                ? request.thermometerTrackerEnabled()
                : false)
        .formflowApiUrl(request.formflowApiUrl())
        .formflowApiVersion(request.formflowApiVersion())
        .formflowEnabled(request.formflowEnabled() != null ? request.formflowEnabled() : false)
        .mailDomains(DelimitedStringUtil.join(request.mailDomains()))
        .build();
  }

  public static CompanyGroupDefinitionResponse toResponse(CompanyGroupEntity entity) {
    return CompanyGroupDefinitionResponse.builder()
        .companyGroupId(entity.getId())
        .groupCode(entity.getGroupCode())
        .name(entity.getName())
        .groupMails(split(entity.getGroupMail()))
        .logoUrl(entity.getLogoUrl())
        .thermometerTrackerApiUrl(entity.getThermometerTrackerApiUrl())
        .thermometerTrackerApiVersion(entity.getThermometerTrackerApiVersion())
        .thermometerTrackerEnabled(entity.getThermometerTrackerEnabled())
        .formflowApiUrl(entity.getFormflowApiUrl())
        .formflowApiVersion(entity.getFormflowApiVersion())
        .formflowEnabled(entity.getFormflowEnabled())
        .mailDomains(split(entity.getMailDomains()))
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

  public static CompanyDefinitionResponse toResponse(
      final CompanyEntity entity, final EnumConfigurationFacade facade) {
    String companyGroupId = entity.getCompanyGroupId();
    return CompanyDefinitionResponse.builder()
        .companyId(entity.getId())
        .companyGroupId(companyGroupId)
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(split(entity.getCompanyMail()))
        .city(entity.getCity() != null ? entity.getCity().resolve(facade, companyGroupId) : null)
        .country(
            entity.getCountry() != null
                ? entity.getCountry().resolve(facade, companyGroupId)
                : null)
        .zone(entity.getZone() != null ? entity.getZone().resolve(facade, companyGroupId) : null)
        .segment1(
            entity.getSegment1() != null
                ? entity.getSegment1().resolve(facade, companyGroupId)
                : null)
        .segment2(
            entity.getSegment2() != null
                ? entity.getSegment2().resolve(facade, companyGroupId)
                : null)
        .segment3(
            entity.getSegment3() != null
                ? entity.getSegment3().resolve(facade, companyGroupId)
                : null)
        .segment4(
            entity.getSegment4() != null
                ? entity.getSegment4().resolve(facade, companyGroupId)
                : null)
        .segment5(
            entity.getSegment5() != null
                ? entity.getSegment5().resolve(facade, companyGroupId)
                : null)
        .isActive(entity.isActive())
        .build();
  }

  public static AuthUserCompanyResponse toAuthResponse(
      final CompanyEntity entity, final EnumConfigurationFacade facade) {
    return AuthUserCompanyResponse.builder()
        .companyId(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(split(entity.getCompanyMail()))
        .city(entity.getCity())
        .country(entity.getCountry())
        .zone(entity.getZone())
        .segment1(
            entity.getSegment1() != null
                ? entity.getSegment1().resolve(facade, entity.getId())
                : null)
        .segment2(
            entity.getSegment2() != null
                ? entity.getSegment2().resolve(facade, entity.getId())
                : null)
        .segment3(
            entity.getSegment3() != null
                ? entity.getSegment3().resolve(facade, entity.getId())
                : null)
        .segment4(
            entity.getSegment4() != null
                ? entity.getSegment4().resolve(facade, entity.getId())
                : null)
        .segment5(
            entity.getSegment5() != null
                ? entity.getSegment5().resolve(facade, entity.getId())
                : null)
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
        .thermometerTrackerApiUrl(entity.getThermometerTrackerApiUrl())
        .thermometerTrackerApiVersion(entity.getThermometerTrackerApiVersion())
        .thermometerTrackerEnabled(entity.getThermometerTrackerEnabled())
        .formflowApiUrl(entity.getFormflowApiUrl())
        .formflowApiVersion(entity.getFormflowApiVersion())
        .formflowEnabled(entity.getFormflowEnabled())
        .build();
  }
}
