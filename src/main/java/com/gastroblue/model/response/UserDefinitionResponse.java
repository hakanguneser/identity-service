package com.gastroblue.model.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.shared.EnumDisplay;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDefinitionResponse {
  private String userId;
  private String companyId;
  private String companyGroupId;
  private String username;
  private String email;
  private Boolean isActive;
  private String name;
  private String surname;
  private String phone;
  @JsonIgnore private Language language;
  @JsonIgnore private Gender gender;
  @JsonIgnore private Zone zone;
  @JsonIgnore private List<Department> departments;
  @JsonIgnore private ApplicationRole applicationRole;

  @JsonGetter("departments")
  public List<EnumDisplay> getDepartmentDisplay() {
    return EnumDisplay.of(departments);
  }

  @JsonGetter("applicationRole")
  public EnumDisplay getApplicationRoleDisplay() {
    return EnumDisplay.of(applicationRole);
  }

  @JsonGetter("language")
  public EnumDisplay getLanguageDisplay() {
    return EnumDisplay.of(language);
  }

  @JsonGetter("gender")
  public EnumDisplay getGenderDisplay() {
    return EnumDisplay.of(gender);
  }

  @JsonGetter("zone")
  public EnumDisplay getZoneDisplay() {
    return EnumDisplay.of(zone);
  }
}
