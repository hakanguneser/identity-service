package com.gastroblue.mapper;

import static com.gastroblue.util.DelimitedStringUtil.splitClean;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.EnumTypes;
import com.gastroblue.model.response.tracker.TrackerCompany;
import com.gastroblue.model.response.tracker.TrackerCompanyContextResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyGroup;
import com.gastroblue.model.response.tracker.TrackerUser;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.util.DelimitedStringUtil;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrackerMapper {

  public static TrackerUser toUser(
      UserEntity entity, UserProductEntity userProduct, EnumConfigurationFacade facade) {
    if (entity == null) {
      return null;
    }
    List<String> departmentKeys =
        userProduct != null ? splitClean(userProduct.getDepartments()) : Collections.emptyList();
    List<ResolvedEnum> resolvedDepartmentList =
        departmentKeys.stream()
            .map(d -> facade.resolve(EnumTypes.DEPARTMENT, d, entity.getCompanyGroupId()))
            .toList();
    return TrackerUser.builder()
        .userId(entity.getId())
        .companyId(entity.getCompanyId())
        .companyGroupId(entity.getCompanyGroupId())
        .username(entity.getUsername())
        .departments(resolvedDepartmentList)
        .applicationRole(
            userProduct != null ? userProduct.getApplicationRole().toResolvedEnum() : null)
        .language(
            facade.resolve(
                EnumTypes.LANGUAGE, entity.getLanguage().name(), entity.getCompanyGroupId()))
        .email(entity.getEmail())
        .isActive(userProduct != null ? userProduct.isActive() : entity.isActive())
        .name(entity.getName())
        .surname(entity.getSurname())
        .phone(entity.getPhone())
        .gender(
            entity.getGender() != null
                ? facade.resolve(EnumTypes.GENDER, entity.getGender(), entity.getCompanyGroupId())
                : null)
        .zone(
            entity.getZone() != null
                ? facade.resolve(EnumTypes.ZONE, entity.getZone(), entity.getCompanyGroupId())
                : null)
        .build();
  }

  public static TrackerCompanyGroup toCompanyGroup(CompanyGroupEntity entity) {
    return TrackerCompanyGroup.builder()
        .companyGroupId(entity.getId())
        .groupCode(entity.getGroupCode())
        .name(entity.getName())
        .groupMails(DelimitedStringUtil.split(entity.getGroupMail()))
        .logoUrl(entity.getLogoUrl())
        .mailDomains(DelimitedStringUtil.split(entity.getMailDomains()))
        .build();
  }

  public static TrackerCompany toCompany(CompanyEntity entity, EnumConfigurationFacade facade) {
    String companyGroupId = entity.getCompanyGroupId();
    return TrackerCompany.builder()
        .companyId(entity.getId())
        .companyGroupId(companyGroupId)
        .companyCode(entity.getCompanyCode())
        .companyName(entity.getCompanyName())
        .companyMail(DelimitedStringUtil.split(entity.getCompanyMail()))
        .city(
            entity.getCity() != null
                ? facade.resolve(EnumTypes.CITY, entity.getCity(), companyGroupId)
                : null)
        .country(
            entity.getCountry() != null
                ? facade.resolve(EnumTypes.COUNTRY, entity.getCountry(), companyGroupId)
                : null)
        .zone(
            entity.getZone() != null
                ? facade.resolve(EnumTypes.ZONE, entity.getZone(), companyGroupId)
                : null)
        .segment1(
            entity.getSegment1() != null
                ? facade.resolve(EnumTypes.SEGMENT_1, entity.getSegment1(), companyGroupId)
                : null)
        .segment2(
            entity.getSegment2() != null
                ? facade.resolve(EnumTypes.SEGMENT_2, entity.getSegment2(), companyGroupId)
                : null)
        .segment3(
            entity.getSegment3() != null
                ? facade.resolve(EnumTypes.SEGMENT_3, entity.getSegment3(), companyGroupId)
                : null)
        .segment4(
            entity.getSegment4() != null
                ? facade.resolve(EnumTypes.SEGMENT_4, entity.getSegment4(), companyGroupId)
                : null)
        .segment5(
            entity.getSegment5() != null
                ? facade.resolve(EnumTypes.SEGMENT_5, entity.getSegment5(), companyGroupId)
                : null)
        .isActive(entity.isActive())
        .build();
  }

  public static TrackerCompanyContextResponse toCompanyContextResponse(
      CompanyGroupEntity group, CompanyEntity company, EnumConfigurationFacade facade) {
    return TrackerCompanyContextResponse.builder()
        .companyGroup(toCompanyGroup(group))
        .company(toCompany(company, facade))
        .build();
  }
}
