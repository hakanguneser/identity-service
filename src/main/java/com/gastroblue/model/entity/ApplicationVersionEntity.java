package com.gastroblue.model.entity;

import com.gastroblue.commons.helper.persistence.model.base.Auditable;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.enums.DevicePlatform;
import jakarta.persistence.*;
import lombok.*;

/** Latest and minimum supported mobile app version of a product on a platform. */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "APPLICATION_VERSIONS")
public class ApplicationVersionEntity extends Auditable {

  @Enumerated(EnumType.STRING)
  @Column(name = "PRODUCT", nullable = false, length = 36)
  private ApplicationProduct product;

  @Enumerated(EnumType.STRING)
  @Column(name = "PLATFORM", nullable = false, length = 10)
  private DevicePlatform platform;

  @Column(name = "LATEST_VERSION", nullable = false, length = 20)
  private String latestVersion;

  @Column(name = "MIN_SUPPORTED_VERSION", nullable = false, length = 20)
  private String minSupportedVersion;

  @Column(name = "STORE_URL", nullable = false, length = 500)
  private String storeUrl;

  @Column(name = "IS_ACTIVE", nullable = false)
  private boolean active;
}
