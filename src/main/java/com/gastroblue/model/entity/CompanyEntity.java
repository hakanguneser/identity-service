package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
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
    name = "COMPANIES",
    uniqueConstraints = {
      @UniqueConstraint(name = "UK_COMPANIES_COMPANY_CODE", columnNames = "COMPANY_CODE")
    },
    indexes = {
      @Index(name = "IDX_COMPANIES_COMPANY_CODE", columnList = "COMPANY_CODE"),
      @Index(name = "IDX_COMPANIES_COMPANY_GROUP_ID", columnList = "COMPANY_GROUP_ID")
    })
public class CompanyEntity extends Auditable {

  @Column(name = "COMPANY_CODE", length = 50)
  private String companyCode;

  @Column(name = "COMPANY_NAME", length = 250)
  private String companyName;

  @Column(name = "COMPANY_GROUP_ID", length = 36)
  private String companyGroupId;

  @Column(name = "COMPANY_MAIL", length = 500)
  private String companyMail;

  @Column(name = "COUNTRY", length = 50)
  private String country;

  @Column(name = "CITY", length = 50)
  private String city;

  @Column(name = "ZONE", length = 50)
  private String zone;

  @Column(name = "SEGMENT_1", length = 50)
  private String segment1;

  @Column(name = "SEGMENT_2", length = 50)
  private String segment2;

  @Column(name = "SEGMENT_3", length = 50)
  private String segment3;

  @Column(name = "SEGMENT_4", length = 50)
  private String segment4;

  @Column(name = "SEGMENT_5", length = 50)
  private String segment5;

  @Column(name = "IS_ACTIVE")
  private boolean active;
}
