package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "COMPANY_GROUPS")
public class CompanyGroupEntity extends Auditable {

  @Column(name = "NAME", length = 100)
  private String name;

  @Column(name = "GROUP_CODE", length = 25, unique = true)
  private String groupCode;

  @Column(name = "GROUP_MAIL", length = 500)
  private String groupMail;

  @Column(name = "LOGO_URL", length = 100)
  private String logoUrl;
}
