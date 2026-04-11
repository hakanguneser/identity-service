package com.gastroblue.model.response.tracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerCompanyContextResponse {
  private TrackerCompanyGroup companyGroup;
  private TrackerCompany company;
}
