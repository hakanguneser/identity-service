package com.gastroblue.mapper;

import static com.gastroblue.commons.shared.util.DelimitedStringUtil.join;
import static com.gastroblue.commons.shared.util.DelimitedStringUtil.split;

import com.gastroblue.commons.helper.lookup.model.dto.LookupQuery;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.model.base.Company;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.CompanyGroupProductEntity;
import com.gastroblue.model.entity.CompanyProductEntity;
import com.gastroblue.model.enums.Lookups;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanySaveRequest;
import com.gastroblue.model.response.AuthUserCompanyGroupResponse;
import com.gastroblue.model.response.AuthUserCompanyResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupProductResponse;
import com.gastroblue.model.response.CompanyProductResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CompanyGroupMapper {

  public static CompanyGroupEntity toEntity(CompanyGroupSaveRequest request) {
    return CompanyGroupEntity.builder()
        .name(request.name())
        .groupCode(request.groupCode())
        .groupMail(join(request.groupMails()))
        .logoUrl(request.logoUrl())
        .mailDomains(join(request.mailDomains()))
        .build();
  }

  public static CompanyGroupDefinitionResponse toResponse(CompanyGroupEntity entity) {
    return CompanyGroupDefinitionResponse.builder()
        .companyGroupId(entity.getId())
        .groupCode(entity.getGroupCode())
        .name(entity.getName())
        .groupMails(split(entity.getGroupMail()))
        .logoUrl(entity.getLogoUrl())
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
      final CompanyEntity entity, final ILookupService lookupService) {
    LookupQuery base = LookupQuery.of().companyGroupId(entity.getCompanyGroupId());
    return CompanyDefinitionResponse.builder()
        .companyId(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(split(entity.getCompanyMail()))
        .country(lookupService.find(base.lookup(Lookups.COUNTRY).key(entity.getCountry())))
        .city(
            lookupService.find(
                base.lookup(Lookups.CITY).parentKey(entity.getCountry()).key(entity.getCity())))
        .zone(lookupService.find(base.lookup(Lookups.ZONE).key(entity.getZone())))
        .segment1(lookupService.find(base.lookup(Lookups.SEGMENT_1).key(entity.getSegment1())))
        .segment2(lookupService.find(base.lookup(Lookups.SEGMENT_2).key(entity.getSegment2())))
        .segment3(lookupService.find(base.lookup(Lookups.SEGMENT_3).key(entity.getSegment3())))
        .segment4(lookupService.find(base.lookup(Lookups.SEGMENT_4).key(entity.getSegment4())))
        .segment5(lookupService.find(base.lookup(Lookups.SEGMENT_5).key(entity.getSegment5())))
        .isActive(entity.isActive())
        .build();
  }

  public static AuthUserCompanyResponse toAuthResponse(
      final CompanyEntity entity, final ILookupService lookupService) {

    LookupQuery base = LookupQuery.of().companyGroupId(entity.getCompanyGroupId());
    return AuthUserCompanyResponse.builder()
        .companyId(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(split(entity.getCompanyMail()))
        .city(lookupService.find(base.lookup(Lookups.CITY).key(entity.getCity())))
        .country(lookupService.find(base.lookup(Lookups.COUNTRY).key(entity.getCountry())))
        .zone(lookupService.find(base.lookup(Lookups.ZONE).key(entity.getZone())))
        .segment1(lookupService.find(base.lookup(Lookups.SEGMENT_1).key(entity.getSegment1())))
        .segment2(lookupService.find(base.lookup(Lookups.SEGMENT_2).key(entity.getSegment2())))
        .segment3(lookupService.find(base.lookup(Lookups.SEGMENT_3).key(entity.getSegment3())))
        .segment4(lookupService.find(base.lookup(Lookups.SEGMENT_4).key(entity.getSegment4())))
        .segment5(lookupService.find(base.lookup(Lookups.SEGMENT_5).key(entity.getSegment5())))
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

  public static CompanyGroupProductResponse toResponse(CompanyGroupProductEntity entity) {
    return CompanyGroupProductResponse.builder()
        .id(entity.getId())
        .companyGroupId(entity.getCompanyGroupId())
        .product(entity.getProduct())
        .enabled(entity.getEnabled())
        .apiUrl(entity.getApiUrl())
        .apiVersion(entity.getApiVersion())
        .notes(entity.getNotes())
        .build();
  }

  public static CompanyProductResponse toResponse(CompanyProductEntity entity) {
    return CompanyProductResponse.builder()
        .id(entity.getId())
        .companyId(entity.getCompanyId())
        .product(entity.getProduct())
        .enabled(entity.isEnabled())
        .licenseExpiresAt(entity.getLicenseExpiresAt())
        .agreedUserCount(entity.getAgreedUserCount())
        .build();
  }

  public static CompanyGroup toBase(CompanyGroupEntity entity) {
    return CompanyGroup.builder()
        .companyGroupId(entity.getId())
        .name(entity.getName())
        .groupCode(entity.getGroupCode())
        .groupMails(split(entity.getGroupMail()))
        .logoUrl(entity.getLogoUrl())
        .mailDomains(entity.getMailDomains())
        .build();
  }
}
