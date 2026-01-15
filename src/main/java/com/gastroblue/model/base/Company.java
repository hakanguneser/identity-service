package com.gastroblue.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gastroblue.model.enums.*;
import java.util.List;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
  private String companyId;
  @JsonIgnore private String companyGroupId;
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
