package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ApplicationRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "USER_PRODUCTS",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "UK_USER_PRODUCTS",
          columnNames = {"USER_ID", "PRODUCT"})
    },
    indexes = {@Index(name = "IDX_USER_PRODUCTS_USER_ID", columnList = "USER_ID")})
public class UserProductEntity extends Auditable {

  @Column(name = "USER_ID", nullable = false, length = 36)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "PRODUCT", nullable = false, length = 36)
  private ApplicationProduct product;

  @Enumerated(EnumType.STRING)
  @Column(name = "APPLICATION_ROLE", nullable = false, length = 50)
  private ApplicationRole applicationRole;

  @Column(name = "DEPARTMENTS", length = 1000, nullable = false)
  private String departments;

  @Column(name = "IS_ACTIVE", nullable = false)
  private boolean active;

  @Column(name = "LAST_SUCCESS_LOGIN")
  private LocalDateTime lastSuccessLogin;

  @Column(name = "NOTIFICATION_TOKEN", length = 512)
  private String notificationToken;

  @Column(name = "EULA_ACCEPTED_AT")
  private LocalDateTime eulaAcceptedAt;
}
