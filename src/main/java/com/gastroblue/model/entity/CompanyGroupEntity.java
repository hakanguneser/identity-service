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
    name = "COMPANY_GROUPS",
    uniqueConstraints = {
      @UniqueConstraint(name = "UK_COMPANY_GROUPS_COMPANY_CODE", columnNames = "GROUP_CODE")
    },
    indexes = {@Index(name = "IDX_COMPANY_GROUPS_COMPANY_CODE", columnList = "GROUP_CODE")})
public class CompanyGroupEntity extends Auditable {

  @Column(name = "NAME", length = 100)
  private String name;

  @Column(name = "GROUP_CODE", length = 25)
  private String groupCode;

  @Column(name = "GROUP_MAIL", length = 500)
  private String groupMail;

  @Column(name = "LOGO_URL", length = 100)
  private String logoUrl;

  @Column(name = "THERMOMETER_TRACKER_API_URL", length = 512)
  private String thermometerTrackerApiUrl;

  @Column(name = "THERMOMETER_TRACKER_API_VERSION", length = 16)
  private String thermometerTrackerApiVersion;

  @Column(name = "THERMOMETER_TRACKER_ENABLED", nullable = false)
  private Boolean thermometerTrackerEnabled;

  @Column(name = "FORMFLOW_API_URL", length = 512)
  private String formflowApiUrl;

  @Column(name = "FORMFLOW_API_VERSION", length = 16)
  private String formflowApiVersion;

  @Column(name = "FORMFLOW_ENABLED", nullable = false)
  private Boolean formflowEnabled;

  @Column(name = "MAIL_DOMAINS")
  private String mailDomains;
}
