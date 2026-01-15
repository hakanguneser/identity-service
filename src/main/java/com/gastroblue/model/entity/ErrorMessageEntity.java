package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.ErrorCode;
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
    name = "ERROR_MESSAGES",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_app_properties_key_lang",
          columnNames = {"ERROR_CODE", "LANGUAGE"})
    })
public class ErrorMessageEntity extends Auditable {

  @NaturalId
  @Enumerated(EnumType.STRING)
  @Column(name = "ERROR_CODE", nullable = false, length = 500, updatable = false)
  private ErrorCode errorCode;

  @NaturalId
  @Column(name = "LANGUAGE", nullable = false, length = 5, updatable = false)
  @Enumerated(EnumType.STRING)
  private Language language;

  @Column(name = "MESSAGE", nullable = false, length = 1000)
  private String message;
}
