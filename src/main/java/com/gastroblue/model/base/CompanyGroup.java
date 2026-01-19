package com.gastroblue.model.base;

import java.util.List;
import lombok.*;

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
  private String thermometerTrackerApiUrl;
  private String thermometerTrackerApiVersion;
  private boolean thermometerTrackerEnabled;
  private String formflowApiUrl;
  private String formflowApiVersion;
  private boolean formflowEnabled;
}
