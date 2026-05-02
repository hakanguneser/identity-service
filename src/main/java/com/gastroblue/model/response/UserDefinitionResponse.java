package com.gastroblue.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gastroblue.commons.shared.model.DisplayableLookupValue;
import java.util.List;
import lombok.*;

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
  private DisplayableLookupValue language;
  private DisplayableLookupValue gender;
  private DisplayableLookupValue zone;
  private DisplayableLookupValue applicationRole;
  private List<DisplayableLookupValue> departments;
  @JsonIgnore List<String> departmentsList;
}
