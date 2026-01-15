package com.gastroblue.model.base;

import static com.gastroblue.util.DelimitedStringUtil.splitToEnumList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gastroblue.model.enums.*;
import java.util.List;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  private String userId;
  private String companyId;
  private String companyGroupId;
  private String username;
  @JsonIgnore private String password;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private List<Department> departments;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ApplicationRole applicationRole;

  private String email;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Language language;

  private Boolean isActive;
  private String name;
  private String surname;
  private String phone;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String companyName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String companyGroupName;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Gender gender;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Zone zone;

  public User(
      String userId,
      String companyId,
      String companyGroupId,
      String username,
      String password,
      String departments,
      ApplicationRole applicationRole,
      String email,
      Language language,
      Boolean isActive,
      String name,
      String surname,
      String phone,
      Gender gender,
      String companyName,
      String companyGroupName,
      Zone zone) {
    this.userId = userId;
    this.companyId = companyId;
    this.companyGroupId = companyGroupId;
    this.username = username;
    this.password = password;
    this.departments = splitToEnumList(departments, Department.class);
    this.applicationRole = applicationRole;
    this.email = email;
    this.language = language;
    this.isActive = isActive;
    this.name = name;
    this.surname = surname;
    this.phone = phone;
    this.companyGroupName = companyGroupName;
    this.companyName = companyName;
    this.gender = gender;
    this.zone = zone;
  }
}
