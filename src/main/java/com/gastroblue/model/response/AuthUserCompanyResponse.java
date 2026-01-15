package com.gastroblue.model.response;

import com.gastroblue.model.enums.*;
import com.gastroblue.model.shared.ResolvedEnum;
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
  private Country country;
  private City city;
  private Zone zone;
  private ResolvedEnum<CompanySegment1Values> segment1;
  private ResolvedEnum<CompanySegment2Values> segment2;
  private ResolvedEnum<CompanySegment3Values> segment3;
  private ResolvedEnum<CompanySegment4Values> segment4;
  private ResolvedEnum<CompanySegment5Values> segment5;
  private Boolean isActive;
}
