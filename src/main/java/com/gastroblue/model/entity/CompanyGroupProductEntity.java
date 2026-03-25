package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.ApplicationProduct;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "COMPANY_GROUP_PRODUCTS",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UK_COMPANY_GROUP_PRODUCTS",
          columnNames = {"COMPANY_GROUP_ID", "PRODUCT"})
    },
    indexes = {
      @Index(name = "IDX_COMPANY_GROUP_PRODUCTS_GROUP_ID", columnList = "COMPANY_GROUP_ID")
    })
public class CompanyGroupProductEntity extends Auditable {

  @Column(name = "COMPANY_GROUP_ID", nullable = false, length = 36)
  private String companyGroupId;

  @Enumerated(EnumType.STRING)
  @Column(name = "PRODUCT", nullable = false, length = 36)
  private ApplicationProduct product;

  @Column(name = "ENABLED", nullable = false)
  private Boolean enabled;

  @Column(name = "API_URL", length = 512)
  private String apiUrl;

  @Column(name = "API_VERSION", length = 16)
  private String apiVersion;

  @Column(name = "NOTES", length = 500)
  private String notes;
}
