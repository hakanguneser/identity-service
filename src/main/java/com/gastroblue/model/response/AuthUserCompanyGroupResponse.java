package com.gastroblue.model.response;

import lombok.*;

import java.util.List;

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
