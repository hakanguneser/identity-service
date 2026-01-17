package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.*;
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

  @Enumerated(EnumType.STRING)
  @Column(name = "COUNTRY", length = 50)
  private Country country;

  @Enumerated(EnumType.STRING)
  @Column(name = "CITY", length = 50)
  private City city;

  @Enumerated(EnumType.STRING)
  @Column(name = "ZONE", length = 50)
  private Zone zone;

  @Enumerated(EnumType.STRING)
  @Column(name = "SEGMENT_1", length = 50)
  private CompanySegment1Values segment1;

  @Enumerated(EnumType.STRING)
  @Column(name = "SEGMENT_2", length = 50)
  private CompanySegment2Values segment2;

  @Enumerated(EnumType.STRING)
  @Column(name = "SEGMENT_3", length = 50)
  private CompanySegment3Values segment3;

  @Enumerated(EnumType.STRING)
  @Column(name = "SEGMENT_4", length = 50)
  private CompanySegment4Values segment4;

  @Enumerated(EnumType.STRING)
  @Column(name = "SEGMENT_5", length = 50)
  private CompanySegment5Values segment5;

  @Column(name = "IS_ACTIVE")
  private boolean active;
}
