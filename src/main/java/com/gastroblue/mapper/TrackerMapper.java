package com.gastroblue.mapper;

import static com.gastroblue.commons.shared.util.DelimitedStringUtil.split;
import static com.gastroblue.commons.shared.util.DelimitedStringUtil.splitClean;

import com.gastroblue.commons.helper.lookup.model.dto.BaseLookupModel;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.Lookups;
import com.gastroblue.model.response.tracker.TrackerCompany;
import com.gastroblue.model.response.tracker.TrackerCompanyContextResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyGroup;
import com.gastroblue.model.response.tracker.TrackerUser;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrackerMapper {

  public static TrackerUser toUser(
      UserEntity entity, UserProductEntity userProduct, ILookupService lookupService) {
    if (entity == null) {
      return null;
    }
    List<String> departmentKeys =
        userProduct != null ? splitClean(userProduct.getDepartments()) : Collections.emptyList();
    List<BaseLookupModel> resolvedDepartmentList =
        departmentKeys.stream()
            .map(dept -> lookupService.findLookupByKey(Lookups.DEPARTMENT, dept))
            .toList();
    return TrackerUser.builder()
        .userId(entity.getId())
        .companyId(entity.getCompanyId())
        .companyGroupId(entity.getCompanyGroupId())
        .username(entity.getUsername())
        .departments(resolvedDepartmentList)
        .applicationRole(userProduct != null ? userProduct.getApplicationRole().toDisplay() : null)
        .language(lookupService.findLookupByKey(Lookups.LANGUAGE, entity.getLanguage().name()))
        .email(entity.getEmail())
        .isActive(userProduct != null ? userProduct.isActive() : entity.isActive())
        .name(entity.getName())
        .surname(entity.getSurname())
        .phone(entity.getPhone())
        .gender(
            entity.getGender() != null
                ? lookupService.findLookupByKey(Lookups.GENDER, entity.getGender())
                : null)
        .zone(
            entity.getZone() != null
                ? lookupService.findLookupByKey(Lookups.ZONE, entity.getZone())
                : null)
        .build();
  }

  public static TrackerCompanyGroup toCompanyGroup(CompanyGroupEntity entity) {
    return TrackerCompanyGroup.builder()
        .companyGroupId(entity.getId())
        .groupCode(entity.getGroupCode())
        .name(entity.getName())
        .groupMails(split(entity.getGroupMail()))
        .logoUrl(entity.getLogoUrl())
        .mailDomains(split(entity.getMailDomains()))
        .build();
  }

  public static TrackerCompany toCompany(CompanyEntity entity, ILookupService lookupService) {
    String companyGroupId = entity.getCompanyGroupId();
    return TrackerCompany.builder()
        .companyId(entity.getId())
        .companyGroupId(companyGroupId)
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(split(entity.getCompanyMail()))
        .city(lookupService.findLookupByKey(Lookups.CITY, entity.getCity()))
        .country(lookupService.findLookupByKey(Lookups.COUNTRY, entity.getCountry()))
        .zone(lookupService.findLookupByKey(Lookups.ZONE, entity.getZone()))
        .segment1(lookupService.findLookupByKey(Lookups.SEGMENT_1, entity.getSegment1()))
        .segment2(lookupService.findLookupByKey(Lookups.SEGMENT_2, entity.getSegment2()))
        .segment3(lookupService.findLookupByKey(Lookups.SEGMENT_3, entity.getSegment3()))
        .segment4(lookupService.findLookupByKey(Lookups.SEGMENT_4, entity.getSegment4()))
        .segment5(lookupService.findLookupByKey(Lookups.SEGMENT_5, entity.getSegment5()))
        .isActive(entity.isActive())
        .build();
  }

  public static TrackerCompanyContextResponse toCompanyContextResponse(
      CompanyGroupEntity group, CompanyEntity company, ILookupService lookupService) {
    return TrackerCompanyContextResponse.builder()
        .companyGroup(toCompanyGroup(group))
        .company(toCompany(company, lookupService))
        .build();
  }
}
