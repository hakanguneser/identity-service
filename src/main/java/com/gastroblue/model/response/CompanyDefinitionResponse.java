package com.gastroblue.model.response;

import com.gastroblue.model.enums.*;
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
  private Country country;
  private City city;
  private Zone zone;
  private CompanySegment1Values segment1;
  private CompanySegment2Values segment2;
  private CompanySegment3Values segment3;
  private CompanySegment4Values segment4;
  private CompanySegment5Values segment5;
  private Boolean isActive;
}
