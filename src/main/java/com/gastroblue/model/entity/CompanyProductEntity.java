package com.gastroblue.model.entity;

import com.gastroblue.commons.helper.persistence.model.base.Auditable;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "COMPANY_PRODUCTS",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UK_COMPANY_PRODUCTS",
          columnNames = {"COMPANY_ID", "PRODUCT"})
    },
    indexes = {@Index(name = "IDX_COMPANY_PRODUCTS_COMPANY_ID", columnList = "COMPANY_ID")})
public class CompanyProductEntity extends Auditable {

  @Column(name = "COMPANY_ID", nullable = false, length = 36)
  private String companyId;

  @Enumerated(EnumType.STRING)
  @Column(name = "PRODUCT", nullable = false, length = 36)
  private ApplicationProduct product;

  @Column(name = "ENABLED", nullable = false)
  private boolean enabled;

  @Column(name = "LICENSE_EXPIRES_AT")
  private LocalDate licenseExpiresAt;

  @Column(name = "AGREED_USER_COUNT")
  private Integer agreedUserCount;
}
