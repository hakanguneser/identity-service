package com.gastroblue.model.base;

import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyGroup {
  private String companyGroupId;
  private String name;
  private String groupCode;
  private List<String> groupMails;
  private String logoUrl;
}
