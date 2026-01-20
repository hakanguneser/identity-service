package com.gastroblue.mapper;

import static com.gastroblue.util.DelimitedStringUtil.splitToEnumList;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.base.DefaultConfigurableEnum;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.base.User;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.Department;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.util.DelimitedStringUtil;
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
        .departments(DelimitedStringUtil.join(request.departments()))
        .applicationRole(request.applicationRole())
        .language(Language.defaultLang())
        .email(request.email())
        .active(true)
        .phone(request.phone())
        .name(request.name())
        .surname(request.surname())
        .gender(request.gender())
        .zone(request.zone())
        .termsAcceptanceRequired(true)
        .passwordChangeRequired(true)
        .build();
  }

  public static User toBase(final UserEntity entity) {
    return User.builder()
        .userId(entity.getId())
        .companyId(entity.getCompanyId())
        .companyGroupId(entity.getCompanyGroupId())
        .username(entity.getUsername())
        .password(entity.getPassword())
        .departments(splitToEnumList(entity.getDepartments(), Department.class))
        .applicationRole(entity.getApplicationRole())
        .language(entity.getLanguage())
        .email(entity.getEmail())
        .isActive(entity.isActive())
        .name(entity.getName())
        .surname(entity.getSurname())
        .phone(entity.getPhone())
        .gender(entity.getGender())
        .zone(entity.getZone())
        .build();
  }

  public static SessionUser toSessionUser(final UserEntity entity) {
    return entity == null
        ? null
        : SessionUser.builder()
            .userId(entity.getId())
            .companyId(entity.getCompanyId())
            .companyGroupId(entity.getCompanyGroupId())
            .username(entity.getUsername())
            .password(entity.getPassword())
            .departments(splitToEnumList(entity.getDepartments(), Department.class))
            .applicationRole(entity.getApplicationRole())
            .email(entity.getEmail())
            .language(entity.getLanguage())
            .isActive(entity.isActive())
            .name(entity.getName())
            .surname(entity.getSurname())
            .phone(entity.getPhone())
            .gender(entity.getGender())
            .zone(entity.getZone())
            .build();
  }

  public static User toBase(final SessionUser sessionUser) {
    return User.builder()
        .userId(sessionUser.userId())
        .companyId(sessionUser.companyId())
        .companyGroupId(sessionUser.companyGroupId())
        .username(sessionUser.username())
        .password(sessionUser.password())
        .departments(sessionUser.departments())
        .applicationRole(sessionUser.applicationRole())
        .language(sessionUser.language())
        .email(sessionUser.email())
        .isActive(sessionUser.isActive())
        .name(sessionUser.name())
        .surname(sessionUser.surname())
        .phone(sessionUser.phone())
        .gender(sessionUser.gender())
        .zone(sessionUser.zone())
        .build();
  }

  public static UserDefinitionResponse toResponse(
      final UserEntity entity, EnumConfigurationFacade facade) {
    if (entity == null) {
      return null;
    }

    String companyGroupId =
        entity.getCompanyGroupId() == null
            ? CompanyGroupService.DEFAULT_COMPANY_GROUP_ID
            : entity.getCompanyGroupId();

    List<Department> departmentList = splitToEnumList(entity.getDepartments(), Department.class);
    List<ResolvedEnum<Department>> resolvedDepartmentList =
        departmentList == null
            ? Collections.emptyList()
            : departmentList.stream().map(d -> facade.resolve(d, companyGroupId)).toList();

    return UserDefinitionResponse.builder()
        .userId(entity.getId())
        .companyId(entity.getCompanyId())
        .companyGroupId(entity.getCompanyGroupId())
        .username(entity.getUsername())
        .departments(resolvedDepartmentList)
        .applicationRole(resolve(facade, entity.getApplicationRole(), companyGroupId))
        .language(resolve(facade, entity.getLanguage(), companyGroupId))
        .email(entity.getEmail())
        .isActive(entity.isActive())
        .name(entity.getName())
        .surname(entity.getSurname())
        .phone(entity.getPhone())
        .gender(resolve(facade, entity.getGender(), companyGroupId))
        .zone(resolve(facade, entity.getZone(), companyGroupId))
        .build();
  }

  public static UserEntity updateEntity(final UserEntity e, final UserUpdateRequest r) {
    if (r.departments() != null) e.setDepartments(DelimitedStringUtil.join(r.departments()));
    if (r.language() != null) e.setLanguage(r.language());
    if (r.phone() != null) e.setPhone(emptyToNull(r.phone()));
    if (r.mail() != null) e.setEmail(emptyToNull(r.mail()));
    if (r.zone() != null) e.setZone(r.zone());
    return e;
  }

  private static String emptyToNull(String s) {
    s = s == null ? null : s.trim();
    return (s == null || s.isEmpty()) ? null : s;
  }

  public static <T extends DefaultConfigurableEnum> ResolvedEnum<T> resolve(
      EnumConfigurationFacade facade, T enumValue, String companyGroupId) {
    if (enumValue == null) {
      return null;
    }
    return facade.resolve(enumValue, companyGroupId);
  }
}
