package com.gastroblue.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  private String country;
  private String city;
  private String zone;
  private String segment1;
  private String segment2;
  private String segment3;
  private String segment4;
  private String segment5;
  private Boolean isActive;
}
