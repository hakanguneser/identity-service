package com.gastroblue.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gastroblue.model.base.Company;
import com.gastroblue.model.base.CompanyGroup;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthUserInfoResponse {
  private UserDefinitionResponse user;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<Company> company;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private CompanyGroup companyGroup;
}
