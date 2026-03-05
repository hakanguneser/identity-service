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
  private ResolvedEnum country;
  private ResolvedEnum city;
  private ResolvedEnum zone;
  private ResolvedEnum segment1;
  private ResolvedEnum segment2;
  private ResolvedEnum segment3;
  private ResolvedEnum segment4;
  private ResolvedEnum segment5;
  private Boolean isActive;
}
