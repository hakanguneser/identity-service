package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.Language;
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
    name = "COMPANY_GROUP_EULA_CONTENT",
    indexes = {
      @Index(
          name = "İDX_CG_EULA_ACTİVE_RANGE",
          columnList = "COMPANY_GROUP_ID,PRODUCT, LANGUAGE, START_DATE, END_DATE")
    },
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UK_CG_EULA_CONTENT",
          columnNames = {"COMPANY_GROUP_ID", "PRODUCT"})
    })
public class CompanyGroupEulaContentEntity extends Auditable {

  @Column(name = "COMPANY_GROUP_ID", length = 36)
  private String companyGroupId;

  @Enumerated(EnumType.STRING)
  @Column(name = "PRODUCT", nullable = false, length = 36)
  private ApplicationProduct product;

  @Column(name = "EULA_VERSION", nullable = false, length = 36)
  private String eulaVersion;

  @Enumerated(EnumType.STRING)
  @Column(name = "LANGUAGE", nullable = false, length = 5)
  private Language language;

  @Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "START_DATE", nullable = false)
  private LocalDate startDate;

  @Column(name = "END_DATE")
  private LocalDate endDate;
}
