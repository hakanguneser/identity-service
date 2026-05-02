package com.gastroblue.model.response;

import com.gastroblue.commons.shared.model.DisplayableLookupValue;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthUserCompanyResponse {
  private String companyId;
  private String companyGroupId;
  private String companyCode;
  private String companyName;
  private List<String> companyMail;
  private String country;
  private String city;
  private String zone;
  private DisplayableLookupValue segment1;
  private DisplayableLookupValue segment2;
  private DisplayableLookupValue segment3;
  private DisplayableLookupValue segment4;
  private DisplayableLookupValue segment5;
  private Boolean isActive;
}
