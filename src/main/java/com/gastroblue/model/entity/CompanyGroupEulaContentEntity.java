package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.Language;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "company_group_eula_content",
    indexes = {
      @Index(
          name = "idx_cg_eula_active_range",
          columnList = "company_group_id, language, start_date, end_date")
    })
public class CompanyGroupEulaContentEntity extends Auditable {
  @Column(name = "company_group_id", nullable = false, length = 36)
  private String companyGroupId;

  @Column(nullable = false, length = 36)
  private String eulaVersion;

  @Column(nullable = false, length = 5)
  private Language language;

  @Column(nullable = false)
  private String content;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;
}
