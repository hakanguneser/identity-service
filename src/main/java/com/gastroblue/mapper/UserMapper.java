package com.gastroblue.mapper;

import static com.gastroblue.util.DelimitedStringUtil.splitToEnumList;

import com.gastroblue.facade.EnumConfigurationFacade;
import com.gastroblue.model.base.ConfigurableEnum;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.Department;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.ResolvedEnum;
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
      final String password,
      final List<Department> departmentList) {
    return UserEntity.builder()
        .companyGroupId(companyGroupId)
        .companyId(companyId)
        .username(request.username().toLowerCase(Locale.ENGLISH))
        .password(password)
        .departments(DelimitedStringUtil.join(departmentList))
        .applicationRole(request.applicationRole())
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
      final UserEntity entity, EnumConfigurationFacade facade) {
    if (entity == null) {
      return null;
    }

    List<Department> departmentList = splitToEnumList(entity.getDepartments(), Department.class);
    List<ResolvedEnum> resolvedDepartmentList =
        departmentList == null
            ? Collections.emptyList()
            : departmentList.stream()
                .map(d -> resolve(facade, d, entity.getCompanyGroupId()))
                .toList();

    return UserDefinitionResponse.builder()
        .userId(entity.getId())
        .departmentsList(departmentList)
        .companyId(entity.getCompanyId())
        .companyGroupId(entity.getCompanyGroupId())
        .username(entity.getUsername())
        .departments(resolvedDepartmentList)
        .applicationRole(resolve(facade, entity.getApplicationRole(), entity.getCompanyGroupId()))
        .language(resolve(facade, entity.getLanguage(), entity.getCompanyGroupId()))
        .email(entity.getEmail())
        .isActive(entity.isActive())
        .name(entity.getName())
        .surname(entity.getSurname())
        .phone(entity.getPhone())
        .gender(resolve(facade, entity.getGender(), entity.getCompanyGroupId()))
        .zone(resolve(facade, entity.getZone(), entity.getCompanyGroupId()))
        .build();
  }

  public static UserEntity updateEntity(final UserEntity e, final UserUpdateRequest r) {
    if (r.departments() != null) e.setDepartments(DelimitedStringUtil.join(r.departments()));
    if (r.mail() != null) e.setEmail(emptyToNull(r.mail()));

    if (r.zone() != null) e.setZone(r.zone());
    return e;
  }

  private static String emptyToNull(String s) {
    s = s == null ? null : s.trim();
    return (s == null || s.isEmpty()) ? null : s;
  }

  private static <T extends ConfigurableEnum> ResolvedEnum resolve(
      EnumConfigurationFacade facade, T enumValue, String companyGroupId) {
    if (enumValue == null) {
      return null;
    }
    return facade.resolve(enumValue, companyGroupId);
  }
}
