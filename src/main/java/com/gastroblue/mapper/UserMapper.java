package com.gastroblue.mapper;

import static com.gastroblue.commons.shared.util.DelimitedStringUtil.splitClean;

import com.gastroblue.commons.helper.lookup.model.dto.BaseLookupModel;
import com.gastroblue.commons.helper.lookup.model.dto.LookupQuery;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.shared.enums.Language;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.Lookups;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.UserDefinitionResponse;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

  public static UserEntity toEntity(
      final String companyGroupId,
      final String companyId,
      final UserSaveRequest request,
      final String password) {
    return UserEntity.builder()
        .companyGroupId(companyGroupId)
        .companyId(companyId)
        .username(request.username().toLowerCase(Locale.ENGLISH))
        .password(password)
        .language(Language.defaultLang())
        .email(request.email())
        .active(true)
        .phone(request.phone())
        .name(request.name())
        .surname(request.surname())
        .gender(request.gender())
        .zone(request.zone())
        .passwordChangeRequired(true)
        .build();
  }

  public static UserDefinitionResponse toResponse(
      final UserEntity entity,
      final UserProductEntity userProduct,
      final ILookupService lookupService) {
    if (entity == null) {
      return null;
    }
    LookupQuery base =
        LookupQuery.of()
            .companyGroupId(entity.getCompanyGroupId())
            .product(userProduct.getProduct().name());
    List<String> departmentKeys =
        userProduct != null ? splitClean(userProduct.getDepartments()) : Collections.emptyList();

    List<BaseLookupModel> resolvedDepartmentList =
        departmentKeys.stream()
            .map(dept -> lookupService.find(base.lookup(Lookups.DEPARTMENT).key(dept)))
            .filter(Objects::nonNull)
            .toList();

    return UserDefinitionResponse.builder()
        .userId(entity.getId())
        .departmentsList(departmentKeys)
        .companyId(entity.getCompanyId())
        .companyGroupId(entity.getCompanyGroupId())
        .username(entity.getUsername())
        .departments(resolvedDepartmentList)
        .applicationRole(
            lookupService.find(
                base.lookup(Lookups.APPLICATION_ROLE).key(userProduct.getApplicationRole().name())))
        .language(
            lookupService.find(base.lookup(Lookups.LANGUAGE).key(entity.getLanguage().name())))
        .email(entity.getEmail())
        .isActive(userProduct.isActive())
        .name(entity.getName())
        .surname(entity.getSurname())
        .phone(entity.getPhone())
        .gender(lookupService.find(base.lookup(Lookups.GENDER).key(entity.getGender())))
        .zone(lookupService.find(base.lookup(Lookups.ZONE).key(entity.getZone())))
        .build();
  }

  public static UserEntity updateEntity(final UserEntity e, final UserUpdateRequest r) {
    if (r.mail() != null) e.setEmail(emptyToNull(r.mail()));
    if (r.zone() != null) e.setZone(r.zone());
    return e;
  }

  private static String emptyToNull(String s) {
    s = s == null ? null : s.trim();
    return (s == null || s.isEmpty()) ? null : s;
  }
}
