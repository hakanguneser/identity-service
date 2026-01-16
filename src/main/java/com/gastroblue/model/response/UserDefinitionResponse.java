package com.gastroblue.model.response;

import com.gastroblue.model.enums.*;
import com.gastroblue.model.shared.ResolvedEnum;
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
  private ResolvedEnum<Language> language;
  private ResolvedEnum<Gender> gender;
  private ResolvedEnum<Zone> zone;
  private List<ResolvedEnum<Department>> departments;
  private ResolvedEnum<ApplicationRole> applicationRole;
}
