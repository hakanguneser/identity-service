package com.gastroblue.model.response;

import java.util.List;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyGroupDefinitionResponse {

  private String companyGroupId;
  private String name;
  private String groupCode;
  private List<String> groupMails;
  private String logoUrl;
  private String thermometerTrackerApiUrl;
  private String thermometerTrackerApiVersion;
  private Boolean thermometerTrackerEnabled;
  private String formflowApiUrl;
  private String formflowApiVersion;
  private Boolean formflowEnabled;
  private List<String> mailDomains;
}
