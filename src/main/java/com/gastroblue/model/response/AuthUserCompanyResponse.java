package com.gastroblue.model.response;

import com.gastroblue.commons.helper.lookup.model.dto.BaseLookupModel;
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
  private BaseLookupModel country;
  private BaseLookupModel city;
  private BaseLookupModel zone;
  private BaseLookupModel segment1;
  private BaseLookupModel segment2;
  private BaseLookupModel segment3;
  private BaseLookupModel segment4;
  private BaseLookupModel segment5;
  private Boolean isActive;
}
