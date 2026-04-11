package com.gastroblue.mapper;

import static com.gastroblue.util.DelimitedStringUtil.splitClean;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.EnumTypes;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
      final EnumConfigurationFacade facade) {
    if (entity == null) {
      return null;
    }

    List<String> departmentKeys =
        userProduct != null ? splitClean(userProduct.getDepartments()) : Collections.emptyList();

    List<ResolvedEnum> resolvedDepartmentList =
        departmentKeys.stream()
            .map(d -> facade.resolve(EnumTypes.DEPARTMENT, d, entity.getCompanyGroupId()))
            .toList();

    return UserDefinitionResponse.builder()
        .userId(entity.getId())
        .departmentsList(departmentKeys)
        .companyId(entity.getCompanyId())
        .companyGroupId(entity.getCompanyGroupId())
        .username(entity.getUsername())
        .departments(resolvedDepartmentList)
        .applicationRole(
            userProduct
                .getApplicationRole()
                .toResolvedEnum()) // TODO: fix burada ApplicationRoleun REsolvedEnum'e
        // dönüştürülmesi gerekiyor
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
