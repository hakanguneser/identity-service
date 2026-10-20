package com.gastroblue.model.response;

import com.gastroblue.commons.helper.lookup.model.dto.BaseLookupModel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDefinitionResponse {
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
