package com.gastroblue.model.entity.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Setter
@Getter
public abstract class Auditable {

  @Id
  @Column(name = "ID", nullable = false, updatable = false, length = 36)
  @UuidGenerator
  @GeneratedValue
  private String id;

  @JsonIgnore
  @CreatedBy
  @Column(name = "CREATED_BY", nullable = false, updatable = false, length = 100)
  protected String createdBy;

  @JsonIgnore
  @CreatedDate
  @Column(name = "CREATED_DATE", nullable = false, updatable = false)
  protected LocalDateTime createdDate;

  @JsonIgnore
  @LastModifiedBy
  @Column(name = "LAST_MODIFIED_BY", length = 100)
  protected String lastModifiedBy;

  @JsonIgnore
  @LastModifiedDate
  @Column(name = "LAST_MODIFIED_DATE")
  protected LocalDateTime lastModifiedDate;

  @JsonIgnore
  @Version
  @Column(name = "VERSION", nullable = false)
  private Long version;
}
