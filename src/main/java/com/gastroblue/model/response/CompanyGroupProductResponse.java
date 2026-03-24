package com.gastroblue.model.response;

import com.gastroblue.model.enums.ApplicationProduct;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyGroupProductResponse {

  private String id;
  private String companyGroupId;
  private ApplicationProduct product;
  private Boolean enabled;
  private LocalDate licenseExpiresAt;
  private Integer agreedUserCount;
  private String apiUrl;
  private String apiVersion;
  private String notes;
}
