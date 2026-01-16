package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enum_value_configuration")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumValueConfiguration extends Auditable {

  private String companyGroupId;

  private String enumType;

  private String enumKey;

  private Language language;

  private String label;

  @Builder.Default private boolean active = true;
}
