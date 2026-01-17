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
}
