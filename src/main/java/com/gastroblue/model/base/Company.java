package com.gastroblue.model.base;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.shared.EnumDisplay;
import lombok.*;

import java.util.List;

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
  @JsonIgnore private Country country;
  @JsonIgnore private City city;
  @JsonIgnore private Zone zone;
  @JsonIgnore private CompanySegment1Values segment1;
  @JsonIgnore private CompanySegment2Values segment2;
  @JsonIgnore private CompanySegment3Values segment3;
  @JsonIgnore private CompanySegment4Values segment4;
  @JsonIgnore private CompanySegment5Values segment5;
  private Boolean isActive;

  @JsonGetter("country")
  public EnumDisplay getCountryDisplay() {
    return EnumDisplay.of(country);
  }

  @JsonGetter("city")
  public EnumDisplay getCityDisplay() {
    return EnumDisplay.of(city);
  }

  @JsonGetter("zone")
  public EnumDisplay getZoneDisplay() {
    return EnumDisplay.of(zone);
  }

  @JsonGetter("segment1")
  public EnumDisplay getSegment1Display() {
    return EnumDisplay.of(segment1);
  }

  @JsonGetter("segment2")
  public EnumDisplay getSegment2Display() {
    return EnumDisplay.of(segment2);
  }

  @JsonGetter("segment3")
  public EnumDisplay getSegment3Display() {
    return EnumDisplay.of(segment3);
  }

  @JsonGetter("segment4")
  public EnumDisplay getSegment4Display() {
    return EnumDisplay.of(segment4);
  }

  @JsonGetter("segment5")
  public EnumDisplay getSegment5Display() {
    return EnumDisplay.of(segment5);
  }
}
