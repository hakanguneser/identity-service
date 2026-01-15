package com.gastroblue.model.response;

import com.gastroblue.model.enums.*;
import com.gastroblue.model.shared.ResolvedEnum;
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
  private ResolvedEnum<Country> country;
  private ResolvedEnum<City> city;
  private ResolvedEnum<Zone> zone;
  private ResolvedEnum<CompanySegment1Values> segment1;
  private ResolvedEnum<CompanySegment2Values> segment2;
  private ResolvedEnum<CompanySegment3Values> segment3;
  private ResolvedEnum<CompanySegment4Values> segment4;
  private ResolvedEnum<CompanySegment5Values> segment5;
  private Boolean isActive;
}
