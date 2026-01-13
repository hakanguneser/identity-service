package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.Language;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "APPLICATION_PROPERTIES",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_app_properties_key_lang",
          columnNames = {"PROPERTY_KEY", "LANGUAGE"})
    })
public class ApplicationPropertyEntity extends Auditable {

  @NaturalId
  @Column(name = "PROPERTY_KEY", nullable = false, length = 500, updatable = false)
  private String propertyKey;

  @NaturalId
  @Column(name = "LANGUAGE", nullable = false, length = 5, updatable = false)
  @Enumerated(EnumType.STRING)
  private Language language;

  @Column(name = "PROPERTY_VALUE", nullable = false, length = 1000)
  private String propertyValue;

  @Column(name = "DESCRIPTION", length = 1000)
  private String description;
}
