package com.gastroblue.model.response;

import com.gastroblue.model.enums.*;
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
  private Language language;
  private Gender gender;
  private Zone zone;
  private List<Department> departments;
  private ApplicationRole applicationRole;
}
