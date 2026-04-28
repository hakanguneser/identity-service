package com.gastroblue.model.response.tracker;

import io.gastroblue.commons.shared.model.DisplayableLookupValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerCompany {
  private String companyId;
  private String companyGroupId;
  private String companyCode;
  private String companyName;
  private List<String> companyMail;
  private DisplayableLookupValue country;
  private DisplayableLookupValue city;
  private DisplayableLookupValue zone;
  private DisplayableLookupValue segment1;
  private DisplayableLookupValue segment2;
  private DisplayableLookupValue segment3;
  private DisplayableLookupValue segment4;
  private DisplayableLookupValue segment5;
  private Boolean isActive;
}
