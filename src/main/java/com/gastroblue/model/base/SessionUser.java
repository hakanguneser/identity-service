package com.gastroblue.model.base;

import static com.gastroblue.service.IJwtService.ANONYMOUS_USER;

import com.gastroblue.exception.ValidationException;
import com.gastroblue.model.enums.*;
import java.util.List;

public record SessionUser(
    String userId,
    String companyId,
    String companyGroupId,
    String username,
    String password,
    List<Department> departments,
    ApplicationRole applicationRole,
    String email,
    Language language,
    Boolean isActive,
    String name,
    String surname,
    String phone,
    Gender gender,
    Zone zone,
    String userFullName) {
  public void checkUserCompanyId(String companyId) {
    if (this.companyId() != null && !companyId.equals(this.companyId())) {
      throw new ValidationException(
          ErrorCode.COMPANY_MISMATCH,
          String.format(
              "User companyId mismatch. User: %s, companyId: %s", this.username(), companyId));
    }
  }

  public String findCurrentUserMail() {
    if (this.email() == null) {
      throw new ValidationException(
          ErrorCode.USER_EMAIL_NOT_FOUND,
          String.format("User email not found. User: %s", this.username()));
    }
    return this.email();
  }

  public String findSessionUserFullName() {
    if (this.name() != null || this.surname() != null) {
      return this.userFullName();
    }
    return ANONYMOUS_USER;
  }

  public boolean hasAllDepartmentAccess() {
    return departments.contains(Department.ALL);
  }

  public String getSessionUsername() {
    if (this.username() != null) {
      return this.username();
    }
    return ANONYMOUS_USER;
  }

  public Language getSessionLanguage() {
    Language language = Language.defaultLang();
    if (this.language() != null) {
      language = this.language();
    }
    return language;
  }

  public SessionUser {
    departments = departments == null ? List.of() : List.copyOf(departments);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String userId;
    private String companyId;
    private String companyGroupId;
    private String username;
    private String password;
    private List<Department> departments;
    private ApplicationRole applicationRole;
    private String email;
    private Language language;
    private Boolean isActive;
    private String name;
    private String surname;
    private String phone;
    private Gender gender;
    private Zone zone;

    private Builder() {}

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder companyId(String companyId) {
      this.companyId = companyId;
      return this;
    }

    public Builder companyGroupId(String companyGroupId) {
      this.companyGroupId = companyGroupId;
      return this;
    }

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder departments(List<Department> departments) {
      this.departments = departments;
      return this;
    }

    public Builder applicationRole(ApplicationRole applicationRole) {
      this.applicationRole = applicationRole;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder language(Language language) {
      this.language = language;
      return this;
    }

    public Builder isActive(Boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder surname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder phone(String phone) {
      this.phone = phone;
      return this;
    }

    public Builder gender(Gender gender) {
      this.gender = gender;
      return this;
    }

    public Builder zone(Zone zone) {
      this.zone = zone;
      return this;
    }

    public SessionUser build() {
      return new SessionUser(
          userId,
          companyId,
          companyGroupId,
          username,
          password,
          departments,
          applicationRole,
          email,
          language,
          isActive,
          name,
          surname,
          phone,
          gender,
          zone,
          name + " " + surname);
    }
  }
}
