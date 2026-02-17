package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "ENUM_VALUE_CONFIGURATIONS",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UK_ENUM_VALUE_CONFIGURATIONS",
          columnNames = {"COMPANY_GROUP_ID", "ENUM_TYPE", "ENUM_KEY", "LANGUAGE"})
    },
    indexes = {@Index(name = "IDX_COMPANY_GROUP_ID", columnList = "COMPANY_GROUP_ID")})
public class EnumValueConfigurationEntity extends Auditable {
  @Column(name = "COMPANY_GROUP_ID", length = 36)
  private String companyGroupId;

  @Column(name = "ENUM_TYPE", length = 50)
  private String enumType;

  @Column(name = "ENUM_KEY", length = 50)
  private String enumKey;

  @Column(name = "LANGUAGE", length = 5)
  @Enumerated(EnumType.STRING)
  private Language language;

  @Column(name = "LABEL", length = 500)
  private String label;

  @Column(name = "IS_ACTIVE")
  private boolean active;
}
