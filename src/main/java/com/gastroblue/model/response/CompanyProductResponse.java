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
public class CompanyProductResponse {

  private String id;
  private String companyId;
  private ApplicationProduct product;
  private Boolean enabled;
  private LocalDate licenseExpiresAt;
  private Integer agreedUserCount;
}
