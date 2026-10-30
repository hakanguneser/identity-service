package com.gastroblue.model.entity;

import com.gastroblue.commons.helper.persistence.model.base.Auditable;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.Language;
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
@Table(name = "COMPANY_GROUP_EULA_CONTENT")
public class CompanyGroupEulaContentEntity extends Auditable {

  // Null: global text, used by every group that has no text of its own (see findActiveContent).
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
