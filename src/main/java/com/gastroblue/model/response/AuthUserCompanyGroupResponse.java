package com.gastroblue.model.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthUserCompanyGroupResponse {

  private String companyGroupId;
  private String name;
  private String groupCode;
  private List<String> groupMails;
  private String logoUrl;
}
